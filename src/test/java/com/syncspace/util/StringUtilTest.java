package com.syncspace.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StringUtil.
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@DisplayName("StringUtil Tests")
class StringUtilTest {
    
    private final StringUtil stringUtil = new StringUtil();
    
    @Test
    @DisplayName("Should validate correct email")
    void testValidEmail() {
        assertTrue(stringUtil.isValidEmail("user@example.com"));
        assertTrue(stringUtil.isValidEmail("test.user+tag@domain.co.uk"));
    }
    
    @Test
    @DisplayName("Should reject invalid email")
    void testInvalidEmail() {
        assertFalse(stringUtil.isValidEmail("invalid-email"));
        assertFalse(stringUtil.isValidEmail("@example.com"));
        assertFalse(stringUtil.isValidEmail(null));
        assertFalse(stringUtil.isValidEmail(""));
    }
    
    @Test
    @DisplayName("Should validate strong password")
    void testValidPassword() {
        assertTrue(stringUtil.isValidPassword("StrongPass123!"));
        assertTrue(stringUtil.isValidPassword("MyP@ssw0rd2024"));
    }
    
    @Test
    @DisplayName("Should reject weak password")
    void testInvalidPassword() {
        assertFalse(stringUtil.isValidPassword("weak"));
        assertFalse(stringUtil.isValidPassword("NoDigits!"));
        assertFalse(stringUtil.isValidPassword("nospecial123"));
        assertFalse(stringUtil.isValidPassword(null));
    }
    
    @Test
    @DisplayName("Should validate name")
    void testValidName() {
        assertTrue(stringUtil.isValidName("John Doe"));
        assertTrue(stringUtil.isValidName("Ab"));
    }
    
    @Test
    @DisplayName("Should reject invalid name")
    void testInvalidName() {
        assertFalse(stringUtil.isValidName("A"));
        assertFalse(stringUtil.isValidName(""));
        assertFalse(stringUtil.isValidName(null));
    }
}
