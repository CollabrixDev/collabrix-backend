package com.syncspace.factory;

import com.syncspace.dto.CreateWorkspaceRequest;
import com.syncspace.entity.User;
import com.syncspace.entity.Workspace;
import com.syncspace.exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory implementation for creating Workspace entities.
 * 
 * Responsibilities:
 * - Validate workspace data
 * - Initialize members set with owner
 * - Set default values
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Slf4j
@Component
public class WorkspaceFactoryImpl implements IWorkspaceFactory {
    
    private static final int MIN_WORKSPACE_NAME_LENGTH = 1;
    private static final int MAX_WORKSPACE_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    
    @Override
    public Workspace createWorkspaceFromRequest(CreateWorkspaceRequest request, User owner) {
        validateWorkspaceName(request.getName());
        validateWorkspaceDescription(request.getDescription());
        
        log.debug("Creating workspace '{}' for user '{}'", request.getName(), owner.getEmail());
        
        return createWorkspace(request.getName(), request.getDescription(), owner);
    }
    
    @Override
    public Workspace createWorkspace(String name, String description, User owner) {
        validateWorkspaceName(name);
        if (description != null) {
            validateWorkspaceDescription(description);
        }
        
        Workspace workspace = Workspace.builder()
                .name(name.trim())
                .description(description != null ? description.trim() : "")
                .owner(owner)
                .isActive(true)
                .build();
        
        // Add owner as initial member
        workspace.addMember(owner);
        
        log.info("Workspace entity created: {}", workspace.getName());
        return workspace;
    }
    
    /**
     * Validate workspace name.
     */
    private void validateWorkspaceName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Workspace name cannot be empty");
        }
        
        if (name.length() < MIN_WORKSPACE_NAME_LENGTH) {
            throw new InvalidInputException(
                "Workspace name must be at least " + MIN_WORKSPACE_NAME_LENGTH + " character"
            );
        }
        
        if (name.length() > MAX_WORKSPACE_NAME_LENGTH) {
            throw new InvalidInputException(
                "Workspace name cannot exceed " + MAX_WORKSPACE_NAME_LENGTH + " characters"
            );
        }
    }
    
    /**
     * Validate workspace description.
     */
    private void validateWorkspaceDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidInputException(
                "Workspace description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters"
            );
        }
    }
}
