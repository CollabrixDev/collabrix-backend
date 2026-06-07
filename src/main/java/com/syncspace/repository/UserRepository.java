package com.syncspace.repository;

import com.syncspace.entity.User;
import org.springframework.stereotype.Repository;

/**
 * UserRepository provides data access implementation for User entity.
 * 
 * Extends IUserRepository interface with Spring Data JPA capabilities.
 * All methods are inherited from JpaRepository and defined in IUserRepository.
 * 
 * Spring automatically provides implementation through proxy beans.
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 * @see IUserRepository
 */
@Repository
public interface UserRepository extends IUserRepository {
    // All methods inherited from IUserRepository
}
