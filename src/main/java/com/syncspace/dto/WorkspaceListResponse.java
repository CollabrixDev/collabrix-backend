package com.syncspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for workspace list response with pagination info.
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceListResponse {
    
    private List<WorkspaceDTO> workspaces;
    private Integer totalCount;
    private Integer pageSize;
    private Integer currentPage;
}
