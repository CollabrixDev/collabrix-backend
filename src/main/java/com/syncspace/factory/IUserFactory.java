package com.syncspace.factory;

import com.syncspace.dto.SignupRequest;
import com.syncspace.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * IUserFactory defines contract for creating User entities.
 * 
 * This interface provides abstraction for user object creation,
 * allowing for flexible implementation and testing.
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
public interface IUserFactory {

    /**
     * Create a new User entity from signup request.
     * 
     * Handles:
     * - Password encoding
     * - Default role assignment (MEMBER)
     * - Default status (ACTIVE)
     * 
     * @param request SignupRequest with user details
     * @param passwordEncoder Encoder for password hashing
     * @return User entity ready for persistence
     * @throws IllegalArgumentException if request is null or password encoding fails
     */
    User createUserFromSignupRequest(SignupRequest request, PasswordEncoder passwordEncoder);

    /**
     * Create a User entity with all specified details.
     * 
     * @param email User email
     * @param name User name
     * @param encodedPassword Already encoded password
     * @param role User role
     * @param isActive User active status
     * @return User entity ready for persistence
     */
    User createUser(String email, String name, String encodedPassword, 
                   User.UserRole role, Boolean isActive);
}
