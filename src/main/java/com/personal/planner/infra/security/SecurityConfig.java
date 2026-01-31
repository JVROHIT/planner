package com.personal.planner.infra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

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

    @Value("${security.cors.allowed-origins:http://localhost:3000}")
    private String corsAllowedOrigins;

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
                // Enable CORS for frontend
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

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

    /**
     * Configures CORS for frontend access.
     *
     * <p>Allows the frontend application to make cross-origin requests:</p>
     * <ul>
     *   <li>Origin: http://localhost:3000 (Next.js dev server)</li>
     *   <li>Methods: GET, POST, PUT, DELETE, OPTIONS</li>
     *   <li>Headers: Authorization, Content-Type</li>
     *   <li>Credentials: Enabled for cookie-based auth</li>
     * </ul>
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
