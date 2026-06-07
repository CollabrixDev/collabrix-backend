package com.syncspace.repository;

import com.syncspace.entity.User;
import com.syncspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Workspace entity.
 * 
 * Provides custom query methods for workspace operations:
 * - Find workspaces by owner
 * - Find workspaces where user is member
 * - Find workspaces by name
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Repository
public interface IWorkspaceRepository extends JpaRepository<Workspace, Long> {
    
    /**
     * Find all active workspaces owned by a user.
     */
    List<Workspace> findByOwnerIdAndIsActiveTrue(Long ownerId);
    
    /**
     * Find workspace by name owned by specific user.
     * Used to prevent duplicate workspace names per user.
     */
    Optional<Workspace> findByNameAndOwnerId(String name, Long ownerId);
    
    /**
     * Find all workspaces where user is a member.
     */
    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE m.id = :userId AND w.isActive = true")
    List<Workspace> findWorkspacesByMemberId(@Param("userId") Long userId);
    
    /**
     * Find active workspaces by owner.
     */
    List<Workspace> findByOwnerId(Long ownerId);
    
    /**
     * Check if workspace exists and is active.
     */
    boolean existsByIdAndIsActiveTrue(Long id);
}
