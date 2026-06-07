package com.syncspace.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * JwtAuthenticationFilter validates JWT tokens and sets authentication context.
 * 
 * This filter:
 * - Extracts JWT token from Authorization header (Bearer scheme)
 * - Validates token signature and expiration
 * - Loads user details from database
 * - Sets Spring Security authentication context
 * - Runs once per request (extends OncePerRequestFilter)
 * - Handles all exceptions gracefully without blocking request flow
 * 
 * Dependencies are injected via constructor (no singletons).
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final IUserDetailsService userDetailsService;

    /**
     * Filter request to validate JWT token and set authentication context.
     * 
     * Process:
     * 1. Extract JWT token from Authorization header (Bearer scheme)
     * 2. Validate token signature and expiration
     * 3. Extract user email from token
     * 4. Load user details from database
     * 5. Create authentication token with user details and authorities
     * 6. Set authentication in SecurityContextHolder
     * 7. Continue filter chain
     * 
     * Exceptions are logged but not propagated to allow request processing to continue.
     * Invalid or missing tokens result in request proceeding without authentication.
     * 
     * @param request HttpServletRequest with potential JWT token
     * @param response HttpServletResponse to modify if needed
     * @param filterChain FilterChain to continue request processing
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (jwt != null && jwtUtil.validateToken(jwt)) {
                String email = jwtUtil.extractEmail(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                                userDetails, 
                                null, 
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated user: {}", email);
            }
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header.
     * 
     * Expects header format: Authorization: Bearer {token}
     * 
     * @param request HttpServletRequest containing Authorization header
     * @return JWT token string if present and properly formatted, null otherwise
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
