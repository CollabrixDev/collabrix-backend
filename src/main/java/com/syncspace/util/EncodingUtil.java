package com.syncspace.util;

import org.springframework.stereotype.Component;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * Utility component for encoding and encryption operations.
 * 
 * Provides:
 * - Base64 encoding/decoding
 * - URL safe encoding
 * - String encoding
 * - Future: AES encryption
 * 
 * Note: Password encoding is handled by Spring Security's PasswordEncoder.
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Component
public class EncodingUtil {
    
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
    private static final Base64.Encoder URL_SAFE_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_SAFE_DECODER = Base64.getUrlDecoder();
    
    /**
     * Encode string to Base64.
     */
    public String encodeBase64(String input) {
        if (input == null) {
            return null;
        }
        return BASE64_ENCODER.encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Decode Base64 string.
     */
    public String decodeBase64(String encoded) {
        if (encoded == null) {
            return null;
        }
        try {
            byte[] decodedBytes = BASE64_DECODER.decode(encoded);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Encode string to URL-safe Base64 (no padding).
     */
    public String encodeUrlSafeBase64(String input) {
        if (input == null) {
            return null;
        }
        return URL_SAFE_ENCODER.encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Decode URL-safe Base64 string.
     */
    public String decodeUrlSafeBase64(String encoded) {
        if (encoded == null) {
            return null;
        }
        try {
            byte[] decodedBytes = URL_SAFE_DECODER.decode(encoded);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Generate a URL-safe token (random Base64).
     */
    public String generateToken(int lengthInBytes) {
        byte[] randomBytes = new byte[lengthInBytes];
        new java.security.SecureRandom().nextBytes(randomBytes);
        return URL_SAFE_ENCODER.encodeToString(randomBytes);
    }
    
    /**
     * URL encode a string (for URI parameters).
     */
    public String urlEncode(String value) {
        if (value == null) {
            return null;
        }
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
    }
    
    /**
     * URL decode a string.
     */
    public String urlDecode(String value) {
        if (value == null) {
            return null;
        }
        try {
            return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
    }
}
