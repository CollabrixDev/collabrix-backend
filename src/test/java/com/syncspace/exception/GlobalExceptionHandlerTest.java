package com.syncspace.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 * 
 * Tests:
 * - Transient exceptions return 503 with retry metadata
 * - Non-transient exceptions return appropriate status without retry metadata
 * - Error responses are properly formatted
 * - Retry-After header is set for transient failures
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest mockRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        mockRequest = mock(WebRequest.class);
        when(mockRequest.getDescription(false)).thenReturn("uri=/api/users");
    }

    // ============ TRANSIENT EXCEPTION HANDLER TESTS ============

    @Test
    @DisplayName("Should handle TransientException with retry metadata")
    void testHandleTransientException() {
        TransientException ex = new TransientException("Service temporarily unavailable", 1000);
        
        ResponseEntity<ErrorResponse> response = handler.handleTransientException(ex, mockRequest);
        
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(503, body.getStatus());
        assertEquals("Service temporarily unavailable", body.getMessage());
        assertEquals(true, body.getRetryable());
        assertEquals(1000, body.getSuggestedDelayMs());
        assertEquals("Transient Error - Please Retry", body.getError());
        
        // Check Retry-After header
        assertTrue(response.getHeaders().containsKey("Retry-After"));
        assertEquals("1", response.getHeaders().getFirst("Retry-After"));
    }

    @Test
    @DisplayName("Should handle ExternalServiceException with retry metadata")
    void testHandleExternalServiceException() {
        ExternalServiceException ex = new ExternalServiceException("PaymentAPI", "Connection timeout");
        
        ResponseEntity<ErrorResponse> response = handler.handleTransientException(ex, mockRequest);
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.getRetryable());
        assertEquals(1000, body.getSuggestedDelayMs());
        assertTrue(body.getMessage().contains("PaymentAPI"));
    }

    @Test
    @DisplayName("Should handle DatabaseException with retry metadata")
    void testHandleDatabaseException() {
        DatabaseException ex = new DatabaseException("Connection pool exhausted", 2000);
        
        ResponseEntity<ErrorResponse> response = handler.handleTransientException(ex, mockRequest);
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.getRetryable());
        assertEquals(2000, body.getSuggestedDelayMs());
    }

    // ============ NON-TRANSIENT EXCEPTION HANDLER TESTS ============

    @Test
    @DisplayName("Should handle InvalidInputException without retry")
    void testHandleInvalidInputException() {
        InvalidInputException ex = new InvalidInputException("Email is required");
        
        ResponseEntity<ErrorResponse> response = handler.handleInvalidInput(ex, mockRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Email is required", body.getMessage());
        assertEquals(false, body.getRetryable());
        assertNull(body.getSuggestedDelayMs());
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException without retry")
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User #123 not found");
        
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, mockRequest);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertEquals("User #123 not found", body.getMessage());
        assertEquals(false, body.getRetryable());
        assertEquals("Resource Not Found", body.getError());
    }

    @Test
    @DisplayName("Should handle DuplicateResourceException without retry")
    void testHandleDuplicateResourceException() {
        DuplicateResourceException ex = new DuplicateResourceException("Email already registered");
        
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateResource(ex, mockRequest);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(409, body.getStatus());
        assertEquals("Email already registered", body.getMessage());
        assertEquals(false, body.getRetryable());
        assertEquals("Duplicate Resource", body.getError());
    }

    // ============ ERROR RESPONSE STRUCTURE TESTS ============

    @Test
    @DisplayName("Error response should contain all required fields")
    void testErrorResponseStructure() {
        InvalidInputException ex = new InvalidInputException("Validation failed");
        
        ResponseEntity<ErrorResponse> response = handler.handleInvalidInput(ex, mockRequest);
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        
        // All fields should be populated
        assertNotNull(body.getStatus());
        assertNotNull(body.getMessage());
        assertNotNull(body.getError());
        assertNotNull(body.getTimestamp());
        assertNotNull(body.getPath());
        assertNotNull(body.getRetryable());
    }

    @Test
    @DisplayName("Timestamp should be recent")
    void testErrorResponseTimestamp() {
        InvalidInputException ex = new InvalidInputException("Test");
        LocalDateTime beforeCall = LocalDateTime.now();
        
        ResponseEntity<ErrorResponse> response = handler.handleInvalidInput(ex, mockRequest);
        
        LocalDateTime afterCall = LocalDateTime.now();
        ErrorResponse body = response.getBody();
        
        assertNotNull(body);
        assertNotNull(body.getTimestamp());
        assertTrue(body.getTimestamp().isAfter(beforeCall.minusSeconds(1)));
        assertTrue(body.getTimestamp().isBefore(afterCall.plusSeconds(1)));
    }

    // ============ RETRY COMPARISON TESTS ============

    @Test
    @DisplayName("Transient vs Non-transient should differ in retry metadata")
    void testTransientVsNonTransientRetryMetadata() {
        TransientException transientEx = new TransientException("Temporary issue", 1000);
        NonTransientException nonTransientEx = new InvalidInputException("Permanent issue");
        
        ResponseEntity<ErrorResponse> transientResponse = handler.handleTransientException(transientEx, mockRequest);
        ResponseEntity<ErrorResponse> nonTransientResponse = handler.handleInvalidInput((InvalidInputException) nonTransientEx, mockRequest);
        
        ErrorResponse transientBody = transientResponse.getBody();
        ErrorResponse nonTransientBody = nonTransientResponse.getBody();
        
        assertNotNull(transientBody);
        assertNotNull(nonTransientBody);
        
        // Transient should be retryable
        assertEquals(true, transientBody.getRetryable());
        assertNotNull(transientBody.getSuggestedDelayMs());
        
        // Non-transient should NOT be retryable
        assertEquals(false, nonTransientBody.getRetryable());
        assertNull(nonTransientBody.getSuggestedDelayMs());
    }
}
