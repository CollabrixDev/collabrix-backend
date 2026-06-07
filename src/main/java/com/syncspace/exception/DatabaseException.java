package com.syncspace.exception;

/**
 * Exception thrown when database operations fail temporarily.
 * 
 * This is a TRANSIENT exception - indicates temporary database unavailability.
 * Automatically triggers retry logic with exponential backoff.
 * 
 * Examples:
 * - Connection pool exhaustion
 * - Temporary database lock
 * - Network timeout to database
 * - Temporary database unavailability
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
public class DatabaseException extends TransientException {
    
    public DatabaseException(String message) {
        super(message, 500);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super(message, cause, 500);
    }
    
    public DatabaseException(String message, int suggestedDelayMs) {
        super(message, suggestedDelayMs);
    }
    
    public DatabaseException(String message, Throwable cause, int suggestedDelayMs) {
        super(message, cause, suggestedDelayMs);
    }
}
