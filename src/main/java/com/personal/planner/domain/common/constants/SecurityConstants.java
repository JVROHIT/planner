package com.personal.planner.domain.common.constants;

/**
 * Security-related constants for FocusFlow.
 * <p>
 * This class contains security configuration constants including authentication,
 * authorization, and API path configurations.
 * </p>
 * <p>
 * Note: JWT settings (secret, expiration, issuer) are configured via environment
 * variables and injected through Spring's @Value annotation:
 * - jwt.secret
 * - jwt.expiration
 * - jwt.issuer
 * </p>
 * <p>
 * This approach ensures:
 * - Secrets are not hardcoded in source code
 * - Different values can be used per environment
 * - Sensitive data is properly externalized
 * </p>
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Prevent instantiation
    }

    /**
     * Base path for all authentication endpoints.
     * Example: /api/auth/register, /api/auth/login
     */
    public static final String AUTH_BASE_PATH = "/api/auth";

    /**
     * Registration endpoint path.
     */
    public static final String REGISTER_PATH = "/register";

    /**
     * Login endpoint path.
     */
    public static final String LOGIN_PATH = "/login";

    /**
     * HTTP Header name for JWT token.
     */
    public static final String AUTH_HEADER = "Authorization";

    /**
     * Bearer token prefix in Authorization header.
     * Format: "Bearer {token}"
     */
    public static final String BEARER_PREFIX = "Bearer ";
}
