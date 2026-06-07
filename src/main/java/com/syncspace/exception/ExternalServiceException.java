package com.syncspace.exception;

/**
 * Exception thrown when an external service call fails.
 * 
 * This is a TRANSIENT exception - indicates temporary service unavailability.
 * Automatically triggers retry logic with exponential backoff.
 * 
 * Examples:
 * - Payment gateway timeout
 * - Email service unavailable
 * - Storage service connection failure
 * - Third-party API rate limiting
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
public class ExternalServiceException extends TransientException {
    
    private final String serviceName;
    
    public ExternalServiceException(String serviceName, String message) {
        super("External service '" + serviceName + "' temporarily unavailable: " + message, 1000);
        this.serviceName = serviceName;
    }
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super("External service '" + serviceName + "' temporarily unavailable: " + message, cause, 1000);
        this.serviceName = serviceName;
    }
    
    public ExternalServiceException(String serviceName, String message, int suggestedDelayMs) {
        super("External service '" + serviceName + "' temporarily unavailable: " + message, suggestedDelayMs);
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
}
