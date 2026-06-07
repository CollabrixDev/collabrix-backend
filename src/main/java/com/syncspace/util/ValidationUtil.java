package com.syncspace.util;

import org.springframework.stereotype.Component;

/**
 * Utility component for validation operations.
 * 
 * Centralizes common validation logic:
 * - ID validation
 * - List validation
 * - Null checks
 * - Range validation
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Component
public class ValidationUtil {
    
    /**
     * Validate if ID is valid (not null and positive).
     */
    public boolean isValidId(Long id) {
        return id != null && id > 0;
    }
    
    /**
     * Validate if object is not null.
     */
    public boolean isNotNull(Object obj) {
        return obj != null;
    }
    
    /**
     * Validate if collection is not null or empty.
     */
    public <T> boolean isNotEmpty(java.util.Collection<T> collection) {
        return collection != null && !collection.isEmpty();
    }
    
    /**
     * Validate if string is not null or empty.
     */
    public boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Validate if value is within range (inclusive).
     */
    public boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * Validate if value is within range (inclusive).
     */
    public boolean isInRange(long value, long min, long max) {
        return value >= min && value <= max;
    }
    
    /**
     * Validate if value is positive.
     */
    public boolean isPositive(Number value) {
        if (value == null) {
            return false;
        }
        return value.doubleValue() > 0;
    }
    
    /**
     * Validate if value is non-negative.
     */
    public boolean isNonNegative(Number value) {
        if (value == null) {
            return false;
        }
        return value.doubleValue() >= 0;
    }
}
