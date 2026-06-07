package com.syncspace.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized error response for API endpoints.
 * 
 * Includes retry metadata for transient failures:
 * - retryable: Indicates if the request can be safely retried
 * - suggestedDelayMs: Recommended delay before retry
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private int status;
    private String message;
    private String error;
    private LocalDateTime timestamp;
    private String path;
    
    // Retry metadata for transient failures
    @Builder.Default
    private Boolean retryable = false;
    
    @Builder.Default
    private Integer suggestedDelayMs = null;

}
