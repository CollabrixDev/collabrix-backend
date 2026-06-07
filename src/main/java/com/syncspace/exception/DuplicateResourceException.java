package com.syncspace.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * 
 * This is a NON-TRANSIENT exception - indicates permanent client error.
 * Should NOT be retried. Returns HTTP 409 Conflict.
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
public class DuplicateResourceException extends NonTransientException {
    
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause, HttpStatus.CONFLICT.value());
    }
}
