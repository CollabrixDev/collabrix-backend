package com.syncspace.factory;

import com.syncspace.dto.CreateWorkspaceRequest;
import com.syncspace.entity.User;
import com.syncspace.entity.Workspace;

/**
 * Factory interface for creating Workspace entities.
 * 
 * Encapsulates workspace creation logic including:
 * - Input validation
 * - Default value assignment
 * - Member initialization
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
public interface IWorkspaceFactory {
    
    /**
     * Create a workspace from a CreateWorkspaceRequest.
     * 
     * @param request The workspace creation request
     * @param owner The user creating the workspace
     * @return New Workspace entity with owner as initial member
     */
    Workspace createWorkspaceFromRequest(CreateWorkspaceRequest request, User owner);
    
    /**
     * Create a workspace with all details.
     * 
     * @param name Workspace name
     * @param description Workspace description
     * @param owner Owner of the workspace
     * @return New Workspace entity
     */
    Workspace createWorkspace(String name, String description, User owner);
}
