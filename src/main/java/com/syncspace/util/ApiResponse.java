package com.syncspace.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized API response wrapper.
 * 
 * Wraps all successful API responses with consistent format:
 * {
 *   "success": true,
 *   "data": { ... },
 *   "message": "Operation successful",
 *   "timestamp": "2026-06-07T10:30:00"
 * }
 * 
 * Errors use GlobalExceptionHandler (ErrorResponse) format.
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    
    private boolean success;
    private T data;
    private String message;
    private long timestamp;
    
    /**
     * Create a successful response.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message("Success")
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create a successful response with custom message.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create an empty successful response.
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
