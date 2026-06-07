package com.syncspace.exception;

/**
 * Base exception for non-transient failures that should not be retried.
 * 
 * Non-transient exceptions occur due to permanent issues:
 * - Invalid input (400)
 * - Authentication failures (401)
 * - Authorization failures (403)
 * - Resource not found (404)
 * - Duplicate resource (409)
 * - Invalid state
 * 
 * These exceptions should fail immediately without retry logic.
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
public class NonTransientException extends RuntimeException {
    
    private final int httpStatus;
    
    public NonTransientException(String message) {
        this(message, 400);
    }
    
    public NonTransientException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
    
    public NonTransientException(String message, Throwable cause) {
        this(message, cause, 400);
    }
    
    public NonTransientException(String message, Throwable cause, int httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
}
