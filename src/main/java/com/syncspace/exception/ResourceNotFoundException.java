package com.syncspace.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 * 
 * This is a NON-TRANSIENT exception - indicates permanent client error.
 * Should NOT be retried. Returns HTTP 404 Not Found.
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
public class ResourceNotFoundException extends NonTransientException {
    
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND.value());
    }
}
