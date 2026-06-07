package com.syncspace.security;

import com.syncspace.entity.User;

/**
 * IUserDetailsService interface defines contract for loading user details.
 * 
 * Provides abstraction for user authentication and authorization details loading.
 * Implements Spring's UserDetailsService interface while adding custom methods
 * for user management.
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 * @see org.springframework.security.core.userdetails.UserDetailsService
 */
public interface IUserDetailsService extends org.springframework.security.core.userdetails.UserDetailsService {

    /**
     * Load user entity by email address.
     * 
     * @param email User email address
     * @return User entity with all details
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if user not found
     */
    User loadUserEntityByEmail(String email);

    /**
     * Check if user exists by email.
     * 
     * @param email User email address
     * @return true if user exists, false otherwise
     */
    boolean userExists(String email);
}
