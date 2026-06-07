package com.syncspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for workspace member information.
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceMemberDTO {
    
    private Long userId;
    private String email;
    private String name;
    private Boolean isOwner;
}
