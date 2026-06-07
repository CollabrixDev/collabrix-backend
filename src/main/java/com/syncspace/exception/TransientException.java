package com.syncspace.exception;

/**
 * Base exception for transient failures that can be retried.
 * 
 * Transient exceptions occur due to temporary issues:
 * - Network timeouts
 * - Service temporarily unavailable
 * - Connection pool exhaustion
 * - Rate limiting
 * 
 * These exceptions should trigger retry logic with exponential backoff.
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
public class TransientException extends RuntimeException {
    
    private final int suggestedDelayMs;
    private final int httpStatus;
    
    public TransientException(String message, int suggestedDelayMs) {
        this(message, suggestedDelayMs, 503);
    }
    
    public TransientException(String message, int suggestedDelayMs, int httpStatus) {
        super(message);
        this.suggestedDelayMs = suggestedDelayMs;
        this.httpStatus = httpStatus;
    }
    
    public TransientException(String message, Throwable cause, int suggestedDelayMs) {
        this(message, cause, suggestedDelayMs, 503);
    }
    
    public TransientException(String message, Throwable cause, int suggestedDelayMs, int httpStatus) {
        super(message, cause);
        this.suggestedDelayMs = suggestedDelayMs;
        this.httpStatus = httpStatus;
    }
    
    public int getSuggestedDelayMs() {
        return suggestedDelayMs;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
}
