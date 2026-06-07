package com.syncspace.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EncodingUtil.
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@DisplayName("EncodingUtil Tests")
class EncodingUtilTest {
    
    private final EncodingUtil encodingUtil = new EncodingUtil();
    
    @Test
    @DisplayName("Should encode and decode Base64")
    void testBase64Encoding() {
        String original = "Hello World";
        String encoded = encodingUtil.encodeBase64(original);
        String decoded = encodingUtil.decodeBase64(encoded);
        
        assertNotNull(encoded);
        assertEquals(original, decoded);
    }
    
    @Test
    @DisplayName("Should encode and decode URL-safe Base64")
    void testUrlSafeBase64Encoding() {
        String original = "user+data/special";
        String encoded = encodingUtil.encodeUrlSafeBase64(original);
        String decoded = encodingUtil.decodeUrlSafeBase64(encoded);
        
        assertNotNull(encoded);
        assertFalse(encoded.contains("+"));
        assertFalse(encoded.contains("/"));
        assertEquals(original, decoded);
    }
    
    @Test
    @DisplayName("Should generate secure token")
    void testGenerateToken() {
        String token1 = encodingUtil.generateToken(32);
        String token2 = encodingUtil.generateToken(32);
        
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
        assertTrue(token1.length() > 0);
    }
    
    @Test
    @DisplayName("Should URL encode string")
    void testUrlEncoding() {
        String original = "hello world & special";
        String encoded = encodingUtil.urlEncode(original);
        
        assertNotNull(encoded);
        assertFalse(encoded.contains(" "));
        assertFalse(encoded.contains("&"));
    }
    
    @Test
    @DisplayName("Should handle null gracefully")
    void testNullHandling() {
        assertNull(encodingUtil.encodeBase64(null));
        assertNull(encodingUtil.decodeBase64(null));
        assertNull(encodingUtil.urlEncode(null));
    }
}
