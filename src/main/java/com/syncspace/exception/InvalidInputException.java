package com.syncspace.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when request input validation fails.
 * 
 * This is a NON-TRANSIENT exception - indicates permanent client error.
 * Should NOT be retried. Returns HTTP 400 Bad Request.
 * 
 * This exception is used for cases where:
 * - Required fields are null or empty
 * - Input format is invalid
 * - Constraints are violated
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
public class InvalidInputException extends NonTransientException {

    /**
     * Constructs InvalidInputException with error message.
     * 
     * @param message Descriptive error message
     */
    public InvalidInputException(String message) {
        super(message, HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Constructs InvalidInputException with error message and cause.
     * 
     * @param message Descriptive error message
     * @param cause Root cause exception
     */
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST.value());
    }
}
