package com.syncspace.util;

import com.syncspace.dto.UserDTO;
import com.syncspace.dto.WorkspaceDTO;
import com.syncspace.entity.User;
import com.syncspace.entity.Workspace;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Utility component for converting entities to DTOs.
 * 
 * Centralizes all DTO mapping logic to avoid redundancy across services.
 * Can be injected into any service that needs entity-to-DTO conversion.
 * 
 * Future: Can be replaced with MapStruct for compile-time mapping.
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Component
public class DtoMapper {
    
    /**
     * Convert User entity to UserDTO.
     */
    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
    
    /**
     * Convert Workspace entity to WorkspaceDTO.
     */
    public WorkspaceDTO toWorkspaceDTO(Workspace workspace) {
        if (workspace == null) {
            return null;
        }
        
        return WorkspaceDTO.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .owner(toUserDTO(workspace.getOwner()))
                .members(workspace.getMembers().stream()
                        .map(this::toUserDTO)
                        .collect(Collectors.toSet()))
                .isActive(workspace.getIsActive())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .memberCount(workspace.getMembers().size())
                .build();
    }
}
