package com.syncspace.controller;

import com.syncspace.dto.AuthResponse;
import com.syncspace.dto.LoginRequest;
import com.syncspace.dto.SignupRequest;
import com.syncspace.dto.UserDTO;
import com.syncspace.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController handles authentication and user management REST endpoints.
 * 
 * Provides endpoints for:
 * - User registration (signup)
 * - User authentication (login)
 * - User information retrieval
 * - Current authenticated user context
 * 
 * All endpoints except signup and login require JWT authentication.
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management APIs")
public class AuthController {

    private final IAuthService authService;

    /**
     * Register a new user (signup).
     * 
     * Creates a new user account with the provided credentials. If successful, returns
     * a JWT token that can be used for authenticated requests.
     * 
     * Request validation:
     * - Name: required, 2-100 characters
     * - Email: required, valid email format, unique
     * - Password: required, minimum 6 characters
     * 
     * @param request SignupRequest containing user registration details
     * @return ResponseEntity with AuthResponse (status 201 CREATED)
     * @throws com.syncspace.exception.DuplicateResourceException if email already exists
     * @throws com.syncspace.exception.InvalidInputException if validation fails
     */
    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Create a new user account with email and password")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("POST /auth/signup - User signup request for email: {}", request.getEmail());
        
        try {
            AuthResponse response = authService.signup(request);
            log.info("User signup successful for email: {}", request.getEmail());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Signup failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    /**
     * Authenticate user and generate JWT token (login).
     * 
     * Authenticates user with email and password credentials. On successful
     * authentication, returns a JWT token valid for 24 hours.
     * 
     * Request validation:
     * - Email: required, valid email format
     * - Password: required, non-empty
     * 
     * @param request LoginRequest containing user credentials
     * @return ResponseEntity with AuthResponse (status 200 OK)
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials invalid
     * @throws com.syncspace.exception.ResourceNotFoundException if user not found
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and get JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/login - User login attempt for email: {}", request.getEmail());
        
        try {
            AuthResponse response = authService.login(request);
            log.info("User login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Login failed for email: {}", request.getEmail());
            throw e;
        }
    }

    /**
     * Retrieve information about the currently authenticated user.
     * 
     * This endpoint requires JWT authentication. Returns user profile information
     * for the authenticated user from the JWT token context.
     * 
     * @return ResponseEntity with UserDTO for current authenticated user (status 200 OK)
     * @throws com.syncspace.exception.ResourceNotFoundException if user not found
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get information about the currently authenticated user")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.info("GET /auth/me - Fetch current user info for: {}", email);
        
        try {
            UserDTO user = authService.getCurrentUser(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to fetch current user info for: {}", email, e);
            throw e;
        }
    }

    /**
     * Retrieve user information by user ID.
     * 
     * This endpoint requires JWT authentication. Returns user profile information
     * for the specified user ID.
     * 
     * @param userId ID of the user to retrieve
     * @return ResponseEntity with UserDTO for specified user (status 200 OK)
     * @throws com.syncspace.exception.ResourceNotFoundException if user not found
     * @throws com.syncspace.exception.InvalidInputException if userId is invalid
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve user information by user ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        log.info("GET /auth/users/{} - Fetch user by id", userId);
        
        try {
            UserDTO user = authService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to fetch user with id: {}", userId, e);
            throw e;
        }
    }

}
