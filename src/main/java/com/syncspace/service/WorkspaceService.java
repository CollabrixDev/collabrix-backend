package com.syncspace.service;

import com.syncspace.dto.CreateWorkspaceRequest;
import com.syncspace.dto.UpdateWorkspaceRequest;
import com.syncspace.dto.WorkspaceDTO;
import com.syncspace.dto.WorkspaceListResponse;
import com.syncspace.dto.UserDTO;
import com.syncspace.entity.User;
import com.syncspace.entity.Workspace;
import com.syncspace.exception.InvalidInputException;
import com.syncspace.exception.ResourceNotFoundException;
import com.syncspace.factory.IWorkspaceFactory;
import com.syncspace.repository.IWorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for workspace operations.
 * 
 * Handles:
 * - Workspace CRUD operations
 * - Member management
 * - Authorization checks
 * - DTO conversion
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WorkspaceService implements IWorkspaceService {
    
    private final IWorkspaceRepository workspaceRepository;
    private final IWorkspaceFactory workspaceFactory;
    
    @Override
    public WorkspaceDTO createWorkspace(CreateWorkspaceRequest request, User owner) {
        log.info("Creating workspace '{}' for user '{}'", request.getName(), owner.getEmail());
        
        // Check for duplicate workspace name per user
        workspaceRepository.findByNameAndOwnerId(request.getName(), owner.getId())
                .ifPresent(ws -> {
                    throw new InvalidInputException("Workspace with name '" + request.getName() + "' already exists");
                });
        
        Workspace workspace = workspaceFactory.createWorkspaceFromRequest(request, owner);
        Workspace savedWorkspace = workspaceRepository.save(workspace);
        
        log.info("Workspace created successfully with ID: {}", savedWorkspace.getId());
        return mapToDTO(savedWorkspace);
    }
    
    @Override
    @Transactional(readOnly = true)
    public WorkspaceListResponse getUserWorkspaces(Long userId) {
        log.debug("Fetching workspaces for user '{}'", userId);
        
        // Get workspaces where user is owner or member
        List<Workspace> ownedWorkspaces = workspaceRepository.findByOwnerIdAndIsActiveTrue(userId);
        List<Workspace> memberWorkspaces = workspaceRepository.findWorkspacesByMemberId(userId);
        
        // Combine and remove duplicates
        List<Workspace> allWorkspaces = ownedWorkspaces.stream()
                .collect(Collectors.toList());
        memberWorkspaces.forEach(ws -> {
            if (!allWorkspaces.contains(ws)) {
                allWorkspaces.add(ws);
            }
        });
        
        List<WorkspaceDTO> workspaceDTOs = allWorkspaces.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        log.debug("Found {} workspaces for user '{}'", workspaceDTOs.size(), userId);
        
        return WorkspaceListResponse.builder()
                .workspaces(workspaceDTOs)
                .totalCount(workspaceDTOs.size())
                .pageSize(workspaceDTOs.size())
                .currentPage(1)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public WorkspaceDTO getWorkspaceById(Long workspaceId) {
        log.debug("Fetching workspace with ID: {}", workspaceId);
        
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> {
                    log.warn("Workspace not found with ID: {}", workspaceId);
                    return new ResourceNotFoundException("Workspace not found with ID: " + workspaceId);
                });
        
        return mapToDTO(workspace);
    }
    
    @Override
    public WorkspaceDTO updateWorkspace(Long workspaceId, UpdateWorkspaceRequest request, User currentUser) {
        log.info("Updating workspace '{}' by user '{}'", workspaceId, currentUser.getEmail());
        
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + workspaceId));
        
        // Check authorization - only owner can update
        if (!workspace.isOwner(currentUser)) {
            log.warn("User '{}' attempted to update workspace without ownership", currentUser.getEmail());
            throw new InvalidInputException("Only workspace owner can update workspace");
        }
        
        // Update fields if provided
        if (request.getName() != null && !request.getName().isEmpty()) {
            workspace.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }
        
        Workspace updatedWorkspace = workspaceRepository.save(workspace);
        log.info("Workspace '{}' updated successfully", workspaceId);
        
        return mapToDTO(updatedWorkspace);
    }
    
    @Override
    public void deleteWorkspace(Long workspaceId, User currentUser) {
        log.info("Deleting workspace '{}' by user '{}'", workspaceId, currentUser.getEmail());
        
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + workspaceId));
        
        // Check authorization - only owner can delete
        if (!workspace.isOwner(currentUser)) {
            log.warn("User '{}' attempted to delete workspace without ownership", currentUser.getEmail());
            throw new InvalidInputException("Only workspace owner can delete workspace");
        }
        
        workspace.setIsActive(false);
        workspaceRepository.save(workspace);
        
        log.info("Workspace '{}' deleted successfully", workspaceId);
    }
    
    @Override
    public WorkspaceDTO addMemberToWorkspace(Long workspaceId, User userToAdd, User currentUser) {
        log.info("Adding member '{}' to workspace '{}' by user '{}'", 
                userToAdd.getEmail(), workspaceId, currentUser.getEmail());
        
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + workspaceId));
        
        // Check authorization - only owner can add members
        if (!workspace.isOwner(currentUser)) {
            log.warn("User '{}' attempted to add member without ownership", currentUser.getEmail());
            throw new InvalidInputException("Only workspace owner can add members");
        }
        
        // Check if already a member
        if (workspace.isMember(userToAdd)) {
            throw new InvalidInputException("User is already a member of this workspace");
        }
        
        workspace.addMember(userToAdd);
        Workspace updatedWorkspace = workspaceRepository.save(workspace);
        
        log.info("Member '{}' added to workspace '{}'", userToAdd.getEmail(), workspaceId);
        return mapToDTO(updatedWorkspace);
    }
    
    @Override
    public WorkspaceDTO removeMemberFromWorkspace(Long workspaceId, User userToRemove, User currentUser) {
        log.info("Removing member '{}' from workspace '{}' by user '{}'", 
                userToRemove.getEmail(), workspaceId, currentUser.getEmail());
        
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + workspaceId));
        
        // Check authorization - only owner can remove members
        if (!workspace.isOwner(currentUser)) {
            log.warn("User '{}' attempted to remove member without ownership", currentUser.getEmail());
            throw new InvalidInputException("Only workspace owner can remove members");
        }
        
        // Cannot remove owner
        if (workspace.isOwner(userToRemove)) {
            throw new InvalidInputException("Cannot remove workspace owner");
        }
        
        // Check if member exists
        if (!workspace.isMember(userToRemove)) {
            throw new ResourceNotFoundException("User is not a member of this workspace");
        }
        
        workspace.removeMember(userToRemove);
        Workspace updatedWorkspace = workspaceRepository.save(workspace);
        
        log.info("Member '{}' removed from workspace '{}'", userToRemove.getEmail(), workspaceId);
        return mapToDTO(updatedWorkspace);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isMemberOfWorkspace(Long workspaceId, Long userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElse(null);
        if (workspace == null) {
            return false;
        }
        
        return workspace.getMembers().stream()
                .anyMatch(m -> m.getId().equals(userId));
    }
    
    /**
     * Convert Workspace entity to WorkspaceDTO.
     */
    private WorkspaceDTO mapToDTO(Workspace workspace) {
        return WorkspaceDTO.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .owner(UserDTO.builder()
                        .id(workspace.getOwner().getId())
                        .email(workspace.getOwner().getEmail())
                        .name(workspace.getOwner().getName())
                        .build())
                .members(workspace.getMembers().stream()
                        .map(m -> UserDTO.builder()
                                .id(m.getId())
                                .email(m.getEmail())
                                .name(m.getName())
                                .build())
                        .collect(Collectors.toSet()))
                .isActive(workspace.getIsActive())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .memberCount(workspace.getMembers().size())
                .build();
    }
}
