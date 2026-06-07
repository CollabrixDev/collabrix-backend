package com.syncspace.service;

import com.syncspace.dto.CreateWorkspaceRequest;
import com.syncspace.dto.UpdateWorkspaceRequest;
import com.syncspace.dto.WorkspaceDTO;
import com.syncspace.dto.WorkspaceListResponse;
import com.syncspace.entity.User;

import java.util.List;

/**
 * Service interface for workspace operations.
 * 
 * Responsibilities:
 * - Create workspaces with owner as initial member
 * - List workspaces for authenticated user
 * - Update workspace details
 * - Delete workspaces
 * - Manage workspace members (add/remove)
 * - Authorization checks (owner only)
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
public interface IWorkspaceService {
    
    /**
     * Create a new workspace.
     * 
     * @param request Workspace creation request
     * @param owner User creating the workspace
     * @return Created workspace DTO
     */
    WorkspaceDTO createWorkspace(CreateWorkspaceRequest request, User owner);
    
    /**
     * Get all workspaces for the current user (owned + member of).
     * 
     * @param userId User ID
     * @return List of workspaces
     */
    WorkspaceListResponse getUserWorkspaces(Long userId);
    
    /**
     * Get workspace by ID.
     * 
     * @param workspaceId Workspace ID
     * @return Workspace DTO
     */
    WorkspaceDTO getWorkspaceById(Long workspaceId);
    
    /**
     * Update workspace details.
     * Only owner can update.
     * 
     * @param workspaceId Workspace ID
     * @param request Update request
     * @param currentUser Current user (must be owner)
     * @return Updated workspace DTO
     */
    WorkspaceDTO updateWorkspace(Long workspaceId, UpdateWorkspaceRequest request, User currentUser);
    
    /**
     * Delete a workspace.
     * Only owner can delete.
     * 
     * @param workspaceId Workspace ID
     * @param currentUser Current user (must be owner)
     */
    void deleteWorkspace(Long workspaceId, User currentUser);
    
    /**
     * Add a member to workspace.
     * Only owner can add members.
     * 
     * @param workspaceId Workspace ID
     * @param userToAdd User to add
     * @param currentUser Current user (must be owner)
     * @return Updated workspace DTO
     */
    WorkspaceDTO addMemberToWorkspace(Long workspaceId, User userToAdd, User currentUser);
    
    /**
     * Remove a member from workspace.
     * Only owner can remove members. Cannot remove owner.
     * 
     * @param workspaceId Workspace ID
     * @param userToRemove User to remove
     * @param currentUser Current user (must be owner)
     * @return Updated workspace DTO
     */
    WorkspaceDTO removeMemberFromWorkspace(Long workspaceId, User userToRemove, User currentUser);
    
    /**
     * Check if user is member of workspace.
     * 
     * @param workspaceId Workspace ID
     * @param userId User ID
     * @return true if user is member
     */
    boolean isMemberOfWorkspace(Long workspaceId, Long userId);
}
