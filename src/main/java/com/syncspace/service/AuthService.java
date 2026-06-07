package com.syncspace.service;

import com.syncspace.dto.AuthResponse;
import com.syncspace.dto.LoginRequest;
import com.syncspace.dto.SignupRequest;
import com.syncspace.dto.UserDTO;
import com.syncspace.entity.User;
import com.syncspace.exception.DuplicateResourceException;
import com.syncspace.exception.InvalidInputException;
import com.syncspace.exception.ResourceNotFoundException;
import com.syncspace.factory.IUserFactory;
import com.syncspace.repository.IUserRepository;
import com.syncspace.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthServiceImpl provides implementation for authentication and user management operations.
 * 
 * This service handles:
 * - User registration with duplicate prevention
 * - User authentication with JWT token generation
 * - User information retrieval and validation
 * - Password encoding and security
 * - Transaction management for data consistency
 * - Retry mechanism for resilience
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 * @see IAuthService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final IUserFactory userFactory;

    /**
     * Register a new user with the provided credentials.
     * 
     * Process:
     * 1. Validates input is not null
     * 2. Checks if email already exists in system
     * 3. Encodes password with BCrypt
     * 4. Creates new User entity with MEMBER role
     * 5. Persists to database with transactional guarantee
     * 6. Generates JWT token for immediate use
     * 7. Returns AuthResponse with token and user details
     * 
     * @param request SignupRequest containing name, email, and password
     * @return AuthResponse with JWT token and UserDTO
     * @throws InvalidInputException if request or required fields are null/empty
     * @throws DuplicateResourceException if email already exists in system
     * @throws RuntimeException if database operation fails
     */
    @Override
    @Transactional
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public AuthResponse signup(SignupRequest request) {
        // Validate input
        validateSignupRequest(request);

        log.info("Attempting to register user with email: {}", request.getEmail());

        // Check if user already exists - fail fast
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User already exists with email: {}", request.getEmail());
            throw new DuplicateResourceException("User already exists with email: " + request.getEmail());
        }

        try {
            // Create new user using factory pattern
            User user = userFactory.createUserFromSignupRequest(request, passwordEncoder);

            User savedUser = userRepository.save(user);
            log.info("User registered successfully with id: {}", savedUser.getId());

            // Generate JWT token
            String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getId());

            return AuthResponse.builder()
                    .token(token)
                    .user(UserDTO.fromEntity(savedUser))
                    .message("User registered successfully")
                    .build();

        } catch (DuplicateResourceException | InvalidInputException e) {
            // Re-throw known exceptions without wrapping
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during user registration with email: {}", request.getEmail(), e);
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
        }
    }

    /**
     * Authenticate user with email and password credentials.
     * 
     * Process:
     * 1. Validates login request input
     * 2. Authenticates against Spring Security context
     * 3. Retrieves user from database
     * 4. Generates JWT token with user claims
     * 5. Returns AuthResponse with token and user info
     * 
     * @param request LoginRequest containing email and password
     * @return AuthResponse with JWT token and UserDTO
     * @throws InvalidInputException if request or fields are invalid
     * @throws BadCredentialsException if credentials do not match
     * @throws ResourceNotFoundException if user not found after successful authentication
     */
    @Override
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public AuthResponse login(LoginRequest request) {
        // Validate input
        validateLoginRequest(request);

        log.info("Attempting to login user with email: {}", request.getEmail());

        try {
            // Authenticate using Spring Security with UsernamePasswordAuthenticationToken
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            if (!authentication.isAuthenticated()) {
                log.warn("Authentication failed for user: {}", request.getEmail());
                throw new BadCredentialsException("Invalid email or password");
            }

            // Retrieve user from database
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.error("User not found after successful authentication: {}", request.getEmail());
                        return new ResourceNotFoundException("User not found");
                    });

            // Generate JWT token with user claims
            String token = jwtUtil.generateToken(user.getEmail(), user.getId());
            log.info("User logged in successfully with id: {}", user.getId());

            return AuthResponse.builder()
                    .token(token)
                    .user(UserDTO.fromEntity(user))
                    .message("Login successful")
                    .build();

        } catch (BadCredentialsException | ResourceNotFoundException e) {
            // Re-throw known security exceptions
            log.warn("Login failed for user: {} - {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {}", request.getEmail(), e);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Retrieve current authenticated user information.
     * 
     * @param email Email address of the authenticated user
     * @return UserDTO containing user details
     * @throws InvalidInputException if email is null or empty
     * @throws ResourceNotFoundException if user not found with given email
     */
    @Override
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public UserDTO getCurrentUser(String email) {
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidInputException("Email cannot be null or empty");
        }

        log.debug("Fetching current user info for: {}", email);

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("User not found with email: {}", email);
                        return new ResourceNotFoundException("User not found with email: " + email);
                    });

            return UserDTO.fromEntity(user);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching current user: {}", email, e);
            throw new RuntimeException("Failed to retrieve user information: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve user information by user ID.
     * 
     * @param userId ID of the user to retrieve
     * @return UserDTO containing user details
     * @throws InvalidInputException if userId is null or invalid
     * @throws ResourceNotFoundException if user not found with given ID
     */
    @Override
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public UserDTO getUserById(Long userId) {
        // Validate input
        if (userId == null || userId <= 0) {
            throw new InvalidInputException("User ID must be a positive number");
        }

        log.debug("Fetching user with id: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("User not found with id: {}", userId);
                        return new ResourceNotFoundException("User not found with id: " + userId);
                    });

            return UserDTO.fromEntity(user);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching user by ID: {}", userId, e);
            throw new RuntimeException("Failed to retrieve user information: " + e.getMessage(), e);
        }
    }

    /**
     * Validate signup request for null and empty values.
     * 
     * @param request SignupRequest to validate
     * @throws InvalidInputException if any required field is null or empty
     */
    private void validateSignupRequest(SignupRequest request) {
        if (request == null) {
            throw new InvalidInputException("Signup request cannot be null");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidInputException("Name cannot be null or empty");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new InvalidInputException("Email cannot be null or empty");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new InvalidInputException("Password cannot be null or empty");
        }
    }

    /**
     * Validate login request for null and empty values.
     * 
     * @param request LoginRequest to validate
     * @throws InvalidInputException if any required field is null or empty
     */
    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new InvalidInputException("Login request cannot be null");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new InvalidInputException("Email cannot be null or empty");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new InvalidInputException("Password cannot be null or empty");
        }
    }
}
