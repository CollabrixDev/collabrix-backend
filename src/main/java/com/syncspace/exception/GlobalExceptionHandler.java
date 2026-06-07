package com.syncspace.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler provides centralized exception handling for the application.
 * 
 * Implements the domain-level exception handling pattern:
 * - TRANSIENT exceptions: Retryable failures with retry metadata (exponential backoff)
 * - NON-TRANSIENT exceptions: Permanent failures that should NOT be retried
 * 
 * This handler catches and properly formats all exceptions thrown from:
 * - Controllers and REST endpoints
 * - Services and business logic
 * - Security filters and authentication
 * - Validation failures
 * - External service calls
 * 
 * Each exception is logged appropriately and returned with:
 * - Proper HTTP status codes
 * - Standardized error response format
 * - Retry metadata for transient failures
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle transient exceptions (retryable failures).
     * 
     * Returns 503 Service Unavailable with retry metadata:
     * - retryable: true
     * - suggestedDelayMs: Initial backoff delay for exponential backoff
     * - Retry-After header: For HTTP client compliance
     * 
     * Examples:
     * - External service timeouts
     * - Database connection failures
     * - Network issues
     */
    @ExceptionHandler(TransientException.class)
    public ResponseEntity<ErrorResponse> handleTransientException(
            TransientException ex, 
            WebRequest request) {
        
        log.warn(
            "Transient exception occurred (retryable): {} - {}",
            ex.getClass().getSimpleName(),
            ex.getMessage()
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(ex.getHttpStatus())
                .message(ex.getMessage())
                .error("Transient Error - Please Retry")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .retryable(true)
                .suggestedDelayMs(ex.getSuggestedDelayMs())
                .build();

        return ResponseEntity
                .status(ex.getHttpStatus())
                .header("Retry-After", String.valueOf(ex.getSuggestedDelayMs() / 1000))
                .body(errorResponse);
    }

    /**
     * Handle non-transient exceptions (non-retryable failures).
     * 
     * Returns appropriate HTTP status with NO retry metadata.
     * Should NOT be retried by clients.
     * 
     * Examples:
     * - Invalid input (400)
     * - Duplicate resource (409)
     * - Resource not found (404)
     * - Authentication failures (401)
     */
    @ExceptionHandler(NonTransientException.class)
    public ResponseEntity<ErrorResponse> handleNonTransientException(
            NonTransientException ex, 
            WebRequest request) {
        
        log.debug(
            "Non-transient exception occurred (do not retry): {} - {}",
            ex.getClass().getSimpleName(),
            ex.getMessage()
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(ex.getHttpStatus())
                .message(ex.getMessage())
                .error(mapExceptionToErrorType(ex))
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .retryable(false)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getHttpStatus()));
    }

    /**
     * Handle specific non-transient exceptions (backward compatibility).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, 
            WebRequest request) {
        return handleNonTransientException(ex, request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, 
            WebRequest request) {
        return handleNonTransientException(ex, request);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(
            InvalidInputException ex, 
            WebRequest request) {
        return handleNonTransientException(ex, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, 
            WebRequest request) {
        
        log.warn("Authentication failed: Invalid credentials");
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid email or password")
                .error("Authentication Failed")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .retryable(false)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, 
            WebRequest request) {
        
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .error("Validation Failed")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .retryable(false)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle unexpected exceptions (fallback).
     * 
     * Logs as error and returns 500 Internal Server Error.
     * Should rarely occur if all specific exceptions are handled.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, 
            WebRequest request) {
        
        log.error("Unexpected error occurred: ", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred. Please try again later.")
                .error("Internal Server Error")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .retryable(false)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Map exception type to user-friendly error name.
     */
    private String mapExceptionToErrorType(NonTransientException ex) {
        if (ex instanceof ResourceNotFoundException) {
            return "Resource Not Found";
        } else if (ex instanceof DuplicateResourceException) {
            return "Duplicate Resource";
        } else if (ex instanceof InvalidInputException) {
            return "Invalid Input";
        }
        return "Client Error";
    }
}
