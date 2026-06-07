package com.syncspace.repository;

import com.syncspace.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * IUserRepository defines data access operations for User entity.
 * 
 * Provides contract for:
 * - Basic CRUD operations (inherited from JpaRepository)
 * - Custom query methods for user lookup
 * - Existence checks
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
public interface IUserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address.
     * 
     * @param email User email address
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email address.
     * 
     * @param email User email address
     * @return true if user exists, false otherwise
     */
    Boolean existsByEmail(String email);
}
