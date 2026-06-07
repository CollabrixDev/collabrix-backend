package com.syncspace.controller;

import com.syncspace.dto.CreateWorkspaceRequest;
import com.syncspace.dto.UpdateWorkspaceRequest;
import com.syncspace.dto.WorkspaceDTO;
import com.syncspace.dto.WorkspaceListResponse;
import com.syncspace.entity.User;
import com.syncspace.repository.IUserRepository;
import com.syncspace.service.IWorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for workspace management.
 * 
 * Endpoints:
 * - POST /api/workspaces - Create workspace
 * - GET /api/workspaces - List user's workspaces
 * - GET /api/workspaces/{id} - Get workspace details
 * - PUT /api/workspaces/{id} - Update workspace
 * - DELETE /api/workspaces/{id} - Delete workspace
 * - POST /api/workspaces/{id}/members - Add member
 * - DELETE /api/workspaces/{id}/members/{userId} - Remove member
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspace Management", description = "APIs for managing workspaces")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class WorkspaceController {
    
    private final IWorkspaceService workspaceService;
    private final IUserRepository userRepository;
    
    /**
     * Create a new workspace.
     * 
     * POST /api/workspaces
     * 
     * @param request Workspace creation request
     * @param authentication Current user authentication
     * @return Created workspace
     */
    @PostMapping
    @Operation(summary = "Create a new workspace")
    public ResponseEntity<WorkspaceDTO> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request,
            Authentication authentication) {
        
        log.info("Creating workspace: {}", request.getName());
        
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
        
        WorkspaceDTO workspace = workspaceService.createWorkspace(request, currentUser);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(workspace);
    }
    
    /**
     * Get all workspaces for the current user.
     * 
     * GET /api/workspaces
     * 
     * @param authentication Current user authentication
     * @return List of workspaces
     */
    @GetMapping
    @Operation(summary = "List all workspaces for current user")
    public ResponseEntity<WorkspaceListResponse> getUserWorkspaces(
            Authentication authentication) {
        
        log.info("Fetching workspaces for user: {}", authentication.getName());
        
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
        
        WorkspaceListResponse workspaces = workspaceService.getUserWorkspaces(currentUser.getId());
        
        return ResponseEntity.ok(workspaces);
    }
    
    /**
     * Get workspace by ID.
     * 
     * GET /api/workspaces/{id}
     * 
     * @param id Workspace ID
     * @param authentication Current user authentication
     * @return Workspace details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get workspace details by ID")
    public ResponseEntity<WorkspaceDTO> getWorkspace(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("Fetching workspace: {}", id);
        
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
        
        WorkspaceDTO workspace = workspaceService.getWorkspaceById(id);
        
        // Check if user has access to this workspace
        if (!workspaceService.isMemberOfWorkspace(id, currentUser.getId()) &&
            !workspace.getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(workspace);
    }
    
    /**
     * Update workspace details.
     * Only owner can update.
     * 
     * PUT /api/workspaces/{id}
     * 
     * @param id Workspace ID
     * @param request Update request
     * @param authentication Current user authentication
     * @return Updated workspace
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update workspace (owner only)")
    public ResponseEntity<WorkspaceDTO> updateWorkspace(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkspaceRequest request,
            Authentication authentication) {
        
        log.info("Updating workspace: {}", id);
        
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
        
        WorkspaceDTO workspace = workspaceService.updateWorkspace(id, request, currentUser);
        
        return ResponseEntity.ok(workspace);
    }
    
    /**
     * Delete a workspace.
     * Only owner can delete.
     * 
     * DELETE /api/workspaces/{id}
     * 
     * @param id Workspace ID
     * @param authentication Current user authentication
     * @return No content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workspace (owner only)")
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("Deleting workspace: {}", id);
        
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
        
        workspaceService.deleteWorkspace(id, currentUser);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Add a member to workspace.
     * Only owner can add members.
     * 
     * POST /api/workspaces/{id}/members
     * 
     * @param id Workspace ID
     * @param userId User ID to add
     * @param authentication Current user authentication
     * @return Updated workspace
     */
    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to workspace (owner only)")
    public ResponseEntity<WorkspaceDTO> addMember(
            @PathVariable Long id,
            @RequestParam Long userId,
            Authentication authentication) {
        
        log.info("Adding member {} to workspace {}", userId, id);
        
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
        
        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User to add not found"));
        
        WorkspaceDTO workspace = workspaceService.addMemberToWorkspace(id, userToAdd, currentUser);
        
        return ResponseEntity.ok(workspace);
    }
    
    /**
     * Remove a member from workspace.
     * Only owner can remove members.
     * 
     * DELETE /api/workspaces/{id}/members/{userId}
     * 
     * @param id Workspace ID
     * @param userId User ID to remove
     * @param authentication Current user authentication
     * @return Updated workspace
     */
    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove member from workspace (owner only)")
    public ResponseEntity<WorkspaceDTO> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication authentication) {
        
        log.info("Removing member {} from workspace {}", userId, id);
        
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
        
        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User to remove not found"));
        
        WorkspaceDTO workspace = workspaceService.removeMemberFromWorkspace(id, userToRemove, currentUser);
        
        return ResponseEntity.ok(workspace);
    }
}
