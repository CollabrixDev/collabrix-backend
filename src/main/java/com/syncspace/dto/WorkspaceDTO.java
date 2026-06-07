package com.syncspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for workspace response.
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceDTO {
    
    private Long id;
    private String name;
    private String description;
    private UserDTO owner;
    private Set<UserDTO> members;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer memberCount;
}
