package com.syncspace.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for exception hierarchy and classification.
 * 
 * Tests:
 * - Transient exceptions properly identified as retryable
 * - Non-transient exceptions properly identified as non-retryable
 * - HTTP status codes correctly assigned
 * - Exception messages propagated correctly
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
@DisplayName("Exception Hierarchy Tests")
class ExceptionHierarchyTest {

    // ============ TRANSIENT EXCEPTION TESTS ============

    @Test
    @DisplayName("TransientException should be retryable with suggested delay")
    void testTransientException() {
        int suggestedDelayMs = 1000;
        TransientException ex = new TransientException("Service temporarily unavailable", suggestedDelayMs);
        
        assertEquals("Service temporarily unavailable", ex.getMessage());
        assertEquals(suggestedDelayMs, ex.getSuggestedDelayMs());
        assertEquals(503, ex.getHttpStatus());
    }

    @Test
    @DisplayName("TransientException should support custom HTTP status")
    void testTransientExceptionWithCustomStatus() {
        TransientException ex = new TransientException("Rate limited", 2000, 429);
        
        assertEquals(429, ex.getHttpStatus());
        assertEquals(2000, ex.getSuggestedDelayMs());
    }

    @Test
    @DisplayName("ExternalServiceException should be transient")
    void testExternalServiceException() {
        ExternalServiceException ex = new ExternalServiceException("PaymentGateway", "Connection timeout");
        
        assertTrue(ex instanceof TransientException);
        assertTrue(ex.getMessage().contains("PaymentGateway"));
        assertTrue(ex.getMessage().contains("Connection timeout"));
        assertEquals(1000, ex.getSuggestedDelayMs());
    }

    @Test
    @DisplayName("DatabaseException should be transient")
    void testDatabaseException() {
        DatabaseException ex = new DatabaseException("Connection pool exhausted", 1500);
        
        assertTrue(ex instanceof TransientException);
        assertEquals("Connection pool exhausted", ex.getMessage());
        assertEquals(1500, ex.getSuggestedDelayMs());
    }

    // ============ NON-TRANSIENT EXCEPTION TESTS ============

    @Test
    @DisplayName("NonTransientException should not be retryable")
    void testNonTransientException() {
        NonTransientException ex = new NonTransientException("Invalid input", 400);
        
        assertEquals("Invalid input", ex.getMessage());
        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    @DisplayName("InvalidInputException should be non-transient with 400 status")
    void testInvalidInputException() {
        InvalidInputException ex = new InvalidInputException("Email is required");
        
        assertTrue(ex instanceof NonTransientException);
        assertEquals("Email is required", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getHttpStatus());
    }

    @Test
    @DisplayName("ResourceNotFoundException should be non-transient with 404 status")
    void testResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");
        
        assertTrue(ex instanceof NonTransientException);
        assertEquals("User not found", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getHttpStatus());
    }

    @Test
    @DisplayName("DuplicateResourceException should be non-transient with 409 status")
    void testDuplicateResourceException() {
        DuplicateResourceException ex = new DuplicateResourceException("Email already exists");
        
        assertTrue(ex instanceof NonTransientException);
        assertEquals("Email already exists", ex.getMessage());
        assertEquals(HttpStatus.CONFLICT.value(), ex.getHttpStatus());
    }

    // ============ EXCEPTION HIERARCHY TESTS ============

    @Test
    @DisplayName("All transient exceptions should extend TransientException")
    void testTransientExceptionHierarchy() {
        assertTrue(new ExternalServiceException("test", "msg") instanceof TransientException);
        assertTrue(new DatabaseException("msg") instanceof TransientException);
    }

    @Test
    @DisplayName("All non-transient exceptions should extend NonTransientException")
    void testNonTransientExceptionHierarchy() {
        assertTrue(new InvalidInputException("msg") instanceof NonTransientException);
        assertTrue(new ResourceNotFoundException("msg") instanceof NonTransientException);
        assertTrue(new DuplicateResourceException("msg") instanceof NonTransientException);
    }

    // ============ EXCEPTION WITH CAUSE TESTS ============

    @Test
    @DisplayName("Transient exception should support cause chain")
    void testTransientExceptionWithCause() {
        Throwable cause = new RuntimeException("Original error");
        TransientException ex = new TransientException("Wrapper message", cause, 500);
        
        assertEquals("Wrapper message", ex.getMessage());
        assertEquals(cause, ex.getCause());
        assertEquals(500, ex.getSuggestedDelayMs());
    }

    @Test
    @DisplayName("Non-transient exception should support cause chain")
    void testNonTransientExceptionWithCause() {
        Throwable cause = new IllegalArgumentException("Invalid value");
        NonTransientException ex = new NonTransientException("Validation failed", cause, 400);
        
        assertEquals("Validation failed", ex.getMessage());
        assertEquals(cause, ex.getCause());
        assertEquals(400, ex.getHttpStatus());
    }
}
