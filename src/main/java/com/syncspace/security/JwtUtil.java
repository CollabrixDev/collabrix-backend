package com.syncspace.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtUtil provides utilities for JWT token generation, parsing, and validation.
 * 
 * This utility handles:
 * - Token generation with user claims (email, userId)
 * - Token parsing and claim extraction
 * - Token expiration validation
 * - Signature verification
 * - Comprehensive error handling for token issues
 * 
 * Token Format: Bearer {token}
 * Claims included:
 * - subject (email)
 * - userId (custom claim)
 * - issuedAt (token creation time)
 * - expiration (token expiry time)
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generate a JWT token for authenticated user.
     * 
     * The token contains:
     * - Subject: user email (for authentication identity)
     * - userId claim: user ID (for quick lookup)
     * - Issue time: current timestamp
     * - Expiration: configured expiration time (default 24 hours)
     * - Signature: HMAC SHA-512 with secret key
     * 
     * @param email User email address (used as subject)
     * @param userId User ID (stored as custom claim)
     * @return JWT token string in format suitable for Bearer authentication
     * @throws IllegalArgumentException if email or userId is null
     */
    public String generateToken(String email, Long userId) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("UserId must be a positive number");
        }

        try {
            return Jwts.builder()
                    .subject(email)
                    .claim("userId", userId)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            log.error("Error generating JWT token for user: {}", email, e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    /**
     * Extract user email from JWT token.
     * 
     * Validates token signature and expiration before extraction.
     * 
     * @param token JWT token string
     * @return User email address (token subject)
     * @throws io.jsonwebtoken.ExpiredJwtException if token has expired
     * @throws io.jsonwebtoken.UnsupportedJwtException if token format is not supported
     * @throws io.jsonwebtoken.MalformedJwtException if token is malformed
     * @throws io.jsonwebtoken.security.SignatureException if signature verification fails
     * @throws IllegalArgumentException if token is null or empty
     */
    public String extractEmail(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token");
            throw e;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature");
            throw e;
        } catch (Exception e) {
            log.error("Error extracting email from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extract user ID from JWT token.
     * 
     * Validates token signature and expiration before extraction.
     * 
     * @param token JWT token string
     * @return User ID as Long from 'userId' claim
     * @throws io.jsonwebtoken.ExpiredJwtException if token has expired
     * @throws io.jsonwebtoken.UnsupportedJwtException if token format is not supported
     * @throws io.jsonwebtoken.MalformedJwtException if token is malformed
     * @throws io.jsonwebtoken.security.SignatureException if signature verification fails
     * @throws IllegalArgumentException if token is null or empty
     */
    public Long extractUserId(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("userId", Long.class);
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token");
            throw e;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature");
            throw e;
        } catch (Exception e) {
            log.error("Error extracting userId from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validate JWT token for authenticity and expiration.
     * 
     * Performs the following checks:
     * 1. Signature verification using secret key
     * 2. Expiration time validation
     * 3. Token format validation
     * 
     * @param token JWT token string
     * @return true if token is valid and not expired, false otherwise
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token is null or empty");
            return false;
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            log.debug("Token validation successful");
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error validating token: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get HMAC-SHA512 signing key from configured secret.
     * 
     * Converts the secret string to bytes and creates a SecretKey for JWT signing.
     * 
     * @return SecretKey for HMAC SHA-512 signing
     * @throws IllegalArgumentException if secret key is too short (< 32 bytes)
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            log.warn("JWT secret key is less than 32 bytes, consider increasing key length for production");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
