package com.syncspace.service;

import com.syncspace.dto.AuthResponse;
import com.syncspace.dto.LoginRequest;
import com.syncspace.dto.SignupRequest;
import com.syncspace.dto.UserDTO;

/**
 * AuthService interface defines authentication and user management operations.
 * 
 * This interface provides contract for:
 * - User registration (signup)
 * - User authentication (login)
 * - User information retrieval
 * - Current user context management
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
public interface IAuthService {

    /**
     * Register a new user with the provided credentials.
     * 
     * @param request SignupRequest containing user details (name, email, password)
     * @return AuthResponse with JWT token and user information
     * @throws com.syncspace.exception.DuplicateResourceException if user email already exists
     * @throws com.syncspace.exception.InvalidInputException if validation fails
     */
    AuthResponse signup(SignupRequest request);

    /**
     * Authenticate user with email and password credentials.
     * 
     * @param request LoginRequest containing user credentials (email, password)
     * @return AuthResponse with JWT token and user information
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     * @throws com.syncspace.exception.ResourceNotFoundException if user does not exist
     */
    AuthResponse login(LoginRequest request);

    /**
     * Retrieve current authenticated user information.
     * 
     * @param email Email of the authenticated user
     * @return UserDTO containing user details
     * @throws com.syncspace.exception.ResourceNotFoundException if user not found
     */
    UserDTO getCurrentUser(String email);

    /**
     * Retrieve user information by user ID.
     * 
     * @param userId ID of the user to retrieve
     * @return UserDTO containing user details
     * @throws com.syncspace.exception.ResourceNotFoundException if user not found with given ID
     */
    UserDTO getUserById(Long userId);
}
