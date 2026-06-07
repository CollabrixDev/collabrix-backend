package com.syncspace.config;

import com.syncspace.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * SecurityConfig provides Spring Security configuration for the application.
 * 
 * Configures:
 * - JWT-based stateless authentication
 * - CORS policies for frontend integration
 * - HTTP security rules and access control
 * - Password encoding strategy
 * - Authentication manager
 * - Custom JWT filter chain
 * 
 * All beans are properly configured through factory methods (no singletons).
 * Dependencies are injected via constructor.
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configure password encoding strategy.
     * 
     * Uses BCrypt with strength 12 for production-grade password hashing.
     * This bean is managed by Spring container, not a singleton.
     * 
     * @return PasswordEncoder bean for Spring Security
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("Initializing BCryptPasswordEncoder with strength 12");
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Create AuthenticationManager bean from Spring Security configuration.
     * 
     * Delegates to Spring's default authentication configuration
     * to maintain consistency with framework defaults.
     * 
     * @param config AuthenticationConfiguration from Spring
     * @return AuthenticationManager bean
     * @throws Exception if authentication configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.debug("Creating AuthenticationManager bean");
        return config.getAuthenticationManager();
    }

    /**
     * Configure HTTP security with JWT-based stateless authentication.
     * 
     * Security configuration:
     * - CSRF disabled (using JWT tokens)
     * - Session stateless (no server-side session)
     * - CORS enabled with configured origins
     * - Public endpoints: /auth/signup, /auth/login, /swagger-ui/**, /v3/api-docs/**, /ws/**
     * - Protected endpoints: all other /auth/** endpoints
     * - JWT filter added to filter chain
     * - Unauthorized requests return 401
     * 
     * @param http HttpSecurity to configure
     * @return SecurityFilterChain bean
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public authentication endpoints
                        .requestMatchers("/auth/signup", "/auth/login").permitAll()
                        // Public API documentation endpoints
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // Public actuator endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        // Public WebSocket endpoints
                        .requestMatchers("/ws/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Security filter chain configured successfully");
        return http.build();
    }

    /**
     * Configure CORS (Cross-Origin Resource Sharing) policy.
     * 
     * Allows requests from:
     * - http://localhost:3000 (React development)
     * - http://localhost:5173 (Vite development)
     * 
     * Allowed HTTP methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
     * Exposed headers: Authorization, Content-Type
     * Credentials allowed: true
     * Max age: 1 hour
     * 
     * @return CorsConfigurationSource with CORS policies
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.debug("Creating CORS configuration");

        CorsConfiguration configuration = new CorsConfiguration();
        // Configure allowed origins for development and production
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",    // React dev server
                "http://localhost:5173"     // Vite dev server
        ));
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.debug("CORS configuration created");
        return source;
    }

}
