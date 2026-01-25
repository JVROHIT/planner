package com.personal.planner.infra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for FocusFlow.
 *
 * <p>Configures Spring Security with JWT-based authentication:</p>
 * <ul>
 *   <li>Stateless session management (no server-side sessions)</li>
 *   <li>JWT authentication via JwtAuthenticationFilter</li>
 *   <li>CSRF disabled (not needed for stateless JWT auth)</li>
 * </ul>
 *
 * <p>Authorization rules:</p>
 * <ul>
 *   <li>/api/auth/** - Public (registration and login)</li>
 *   <li>/actuator/health - Public (health checks)</li>
 *   <li>All other endpoints - Require authentication</li>
 * </ul>
 *
 * <p>Filter chain order:</p>
 * <ol>
 *   <li>JwtAuthenticationFilter (extracts and validates JWT)</li>
 *   <li>UsernamePasswordAuthenticationFilter (Spring default, unused)</li>
 *   <li>Authorization filters</li>
 * </ol>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Configures the security filter chain.
     *
     * @param http the HttpSecurity builder
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF - not needed for stateless JWT authentication
                .csrf(csrf -> csrf.disable())

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated())

                // Use stateless sessions - no server-side session state
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Creates the password encoder for user password hashing.
     * Uses BCrypt with default strength (10 rounds).
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
