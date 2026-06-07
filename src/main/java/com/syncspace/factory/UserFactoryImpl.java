package com.syncspace.factory;

import com.syncspace.dto.SignupRequest;
import com.syncspace.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * UserFactoryImpl provides implementation for creating User entities.
 * 
 * This factory:
 * - Encapsulates user entity creation logic
 * - Applies consistent defaults
 * - Handles password encoding
 * - Provides type safety for user creation
 * 
 * Dependency injection handled via constructor.
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 * @see IUserFactory
 */
@Slf4j
@Component
public class UserFactoryImpl implements IUserFactory {

    /**
     * Create a new User entity from signup request.
     * 
     * Process:
     * 1. Validates signup request is not null
     * 2. Validates required fields are not empty
     * 3. Encodes the raw password
     * 4. Builds User entity with MEMBER role
     * 5. Sets active status to true by default
     * 
     * @param request SignupRequest containing name, email, password
     * @param passwordEncoder PasswordEncoder for secure encoding
     * @return User entity ready for database persistence
     * @throws IllegalArgumentException if request is null or fields are invalid
     * @throws RuntimeException if password encoding fails
     */
    @Override
    public User createUserFromSignupRequest(SignupRequest request, PasswordEncoder passwordEncoder) {
        // Validate request
        if (request == null) {
            log.error("SignupRequest is null");
            throw new IllegalArgumentException("SignupRequest cannot be null");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            log.error("User name is null or empty");
            throw new IllegalArgumentException("User name cannot be null or empty");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            log.error("User email is null or empty");
            throw new IllegalArgumentException("User email cannot be null or empty");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            log.error("User password is null or empty");
            throw new IllegalArgumentException("User password cannot be null or empty");
        }

        if (passwordEncoder == null) {
            log.error("PasswordEncoder is null");
            throw new IllegalArgumentException("PasswordEncoder cannot be null");
        }

        try {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            log.debug("Password encoded successfully for email: {}", request.getEmail());

            return User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .role(User.UserRole.MEMBER)
                    .isActive(true)
                    .build();

        } catch (Exception e) {
            log.error("Error creating user from signup request for email: {}", request.getEmail(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    /**
     * Create a User entity with all specified details.
     * 
     * @param email User email address
     * @param name User name
     * @param encodedPassword Pre-encoded password
     * @param role User role (ADMIN or MEMBER)
     * @param isActive User active status
     * @return User entity ready for persistence
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    @Override
    public User createUser(String email, String name, String encodedPassword, 
                          User.UserRole role, Boolean isActive) {
        // Validate parameters
        if (email == null || email.trim().isEmpty()) {
            log.error("Email is null or empty");
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        if (name == null || name.trim().isEmpty()) {
            log.error("Name is null or empty");
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        if (encodedPassword == null || encodedPassword.isEmpty()) {
            log.error("Encoded password is null or empty");
            throw new IllegalArgumentException("Encoded password cannot be null or empty");
        }

        if (role == null) {
            log.error("User role is null");
            throw new IllegalArgumentException("User role cannot be null");
        }

        if (isActive == null) {
            log.error("IsActive flag is null");
            isActive = true; // Default to active
        }

        try {
            return User.builder()
                    .email(email)
                    .name(name)
                    .password(encodedPassword)
                    .role(role)
                    .isActive(isActive)
                    .build();

        } catch (Exception e) {
            log.error("Error creating user with email: {}", email, e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }
}
