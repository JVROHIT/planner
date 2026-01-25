package com.personal.planner.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT authentication filter for FocusFlow.
 *
 * <p>This filter intercepts every request and validates the JWT token
 * in the Authorization header. If valid, it sets the authentication
 * in the SecurityContext.</p>
 *
 * <p>Filter chain behavior:
 * <ol>
 *   <li>Extract Authorization header from request</li>
 *   <li>If no header or not Bearer token, continue filter chain (unauthenticated)</li>
 *   <li>Extract and validate JWT token</li>
 *   <li>If valid, extract userId and set authentication</li>
 *   <li>If invalid or expired, silently continue (unauthenticated)</li>
 *   <li>Continue filter chain</li>
 * </ol>
 * </p>
 *
 * <p>The filter runs once per request (OncePerRequestFilter) to avoid
 * duplicate processing in forward/include scenarios.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /** HTTP header name for Authorization. */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** Bearer token prefix. */
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    /**
     * Filters incoming requests for JWT authentication.
     *
     * <p>Extracts the JWT from the Authorization header, validates it,
     * and sets the authentication context if valid.</p>
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // No Authorization header or not Bearer token - continue unauthenticated
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token (remove "Bearer " prefix)
        final String jwt = authHeader.substring(BEARER_PREFIX.length());

        try {
            final String userId = jwtService.extractUserId(jwt);

            // Set authentication if userId extracted and no existing authentication
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Create authentication token with userId as principal
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.emptyList());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authToken);

                LOG.debug("[JwtFilter] Authenticated user: {}", userId);
            }
        } catch (Exception e) {
            // Token invalid or expired - continue unauthenticated
            // Security layer will handle unauthorized access
            LOG.debug("[JwtFilter] Token validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
