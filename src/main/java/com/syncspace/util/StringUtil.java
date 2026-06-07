package com.syncspace.util;

import org.springframework.stereotype.Component;

/**
 * Utility component for string operations and validation.
 * 
 * Centralizes common string operations:
 * - Email validation
 * - Name validation
 * - String trimming and sanitization
 * - Format checking
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Component
public class StringUtil {
    
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@(.+)$";
    
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;
    
    /**
     * Validate email format.
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
    
    /**
     * Validate password strength.
     * Requirements:
     * - 8-128 characters
     * - At least one uppercase
     * - At least one lowercase
     * - At least one digit
     * - At least one special character
     */
    public boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }
        
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }
    
    /**
     * Validate user name.
     */
    public boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = name.trim();
        return trimmed.length() >= MIN_NAME_LENGTH && trimmed.length() <= MAX_NAME_LENGTH;
    }
    
    /**
     * Safe string trimming - handles null.
     */
    public String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
    
    /**
     * Check if string is null or empty.
     */
    public boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    /**
     * Check if string is null or blank (only whitespace).
     */
    public boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }
    
    /**
     * Truncate string to max length.
     */
    public String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
