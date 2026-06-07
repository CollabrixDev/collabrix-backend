package com.syncspace.security;

import com.syncspace.entity.User;
import com.syncspace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * CustomUserDetailsServiceImpl provides implementation for loading user details from database.
 * 
 * This service:
 * - Loads user information by email from database
 * - Converts user entity to Spring UserDetails
 * - Assigns proper authorities/roles
 * - Handles user not found scenarios
 * - Provides caching-friendly design
 * 
 * Dependencies are injected via constructor (no singletons).
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 * @see IUserDetailsService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements IUserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user details by username (email) for Spring Security.
     * 
     * Implements Spring's UserDetailsService contract.
     * Email is used as the username identifier.
     * 
     * @param email User email address (used as username)
     * @return UserDetails for Spring Security authentication
     * @throws UsernameNotFoundException if user does not exist
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user details for email: {}", email);

        try {
            return userRepository.findByEmail(email)
                    .map(this::convertToUserDetails)
                    .orElseThrow(() -> {
                        log.warn("User not found with email: {}", email);
                        return new UsernameNotFoundException("User not found: " + email);
                    });
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error loading user details for email: {}", email, e);
            throw new RuntimeException("Failed to load user details: " + e.getMessage(), e);
        }
    }

    /**
     * Load user entity by email from database.
     * 
     * Returns the actual user entity object with all JPA mappings intact.
     * 
     * @param email User email address
     * @return User entity from database
     * @throws UsernameNotFoundException if user does not exist
     */
    @Override
    public com.syncspace.entity.User loadUserEntityByEmail(String email) throws UsernameNotFoundException {
        log.debug("Loading user entity for email: {}", email);

        try {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("User entity not found with email: {}", email);
                        return new UsernameNotFoundException("User not found: " + email);
                    });
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error loading user entity for email: {}", email, e);
            throw new RuntimeException("Failed to load user entity: " + e.getMessage(), e);
        }
    }

    /**
     * Check if user exists by email.
     * 
     * @param email User email address
     * @return true if user exists, false otherwise
     */
    @Override
    public boolean userExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.warn("Email parameter is null or empty");
            return false;
        }

        try {
            return userRepository.existsByEmail(email);
        } catch (Exception e) {
            log.error("Error checking user existence for email: {}", email, e);
            return false;
        }
    }

    /**
     * Convert User entity to Spring Security UserDetails.
     * 
     * Maps user entity attributes to UserDetails with proper authorities.
     * 
     * @param user User entity from database
     * @return UserDetails object for Spring Security
     */
    private UserDetails convertToUserDetails(com.syncspace.entity.User user) {
        return new User(
                user.getEmail(),
                user.getPassword(),
                user.getIsActive(),
                true,
                true,
                true,
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                )
        );
    }
}
