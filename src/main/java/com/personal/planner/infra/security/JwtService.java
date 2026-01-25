package com.personal.planner.infra.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for JWT token generation and validation.
 *
 * <p>All configuration is driven by environment variables:
 * <ul>
 *   <li>JWT_SECRET: Secret key for signing tokens (min 32 characters)</li>
 *   <li>JWT_EXPIRATION_MS: Token expiration in milliseconds</li>
 * </ul>
 * </p>
 *
 * <p>This service validates configuration on startup and fails fast if:
 * <ul>
 *   <li>Secret is less than 32 characters</li>
 *   <li>Secret contains "CHANGE_ME" (placeholder value)</li>
 * </ul>
 * </p>
 *
 * <p>Uses HMAC-SHA256 for token signing.</p>
 */
@Component
public class JwtService {

    private static final Logger LOG = LoggerFactory.getLogger(JwtService.class);

    /** Minimum required length for JWT secret. */
    private static final int MIN_SECRET_LENGTH = 32;

    /** Placeholder value that indicates secret hasn't been configured. */
    private static final String PLACEHOLDER_VALUE = "CHANGE_ME";

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    private SecretKey secretKey;

    /**
     * Validates JWT configuration on startup.
     * Fails fast if secret is weak or not configured.
     *
     * @throws IllegalStateException if JWT_SECRET is less than 32 characters
     * @throws IllegalStateException if JWT_SECRET contains CHANGE_ME
     */
    @PostConstruct
    public void validateConfiguration() {
        LOG.info("[JwtService] Validating JWT configuration...");

        // Fail fast if JWT secret is too short
        if (jwtSecret == null || jwtSecret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                "JWT_SECRET must be at least " + MIN_SECRET_LENGTH + " characters. " +
                "Set a strong secret in .env file."
            );
        }

        // Fail fast if JWT secret contains placeholder
        if (jwtSecret.contains(PLACEHOLDER_VALUE)) {
            throw new IllegalStateException(
                "JWT_SECRET contains default placeholder value. " +
                "Set a unique secret in .env file for security."
            );
        }

        // Initialize the secret key
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        LOG.info("[JwtService] JWT configured with expiration: {} ms", jwtExpiration);
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * @param token the JWT token
     * @return the user ID stored in the token subject
     */
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from a JWT token.
     *
     * @param token the JWT token
     * @param claimsResolver function to extract the claim
     * @param <T> the claim type
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a JWT token for a user.
     *
     * @param userId the user ID to include in the token
     * @return the generated JWT token
     */
    public String generateToken(String userId) {
        return generateToken(new HashMap<>(), userId);
    }

    /**
     * Generates a JWT token with extra claims.
     *
     * @param extraClaims additional claims to include
     * @param userId the user ID to include in the token
     * @return the generated JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, String userId) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates a JWT token for a specific user.
     *
     * @param token the JWT token to validate
     * @param userId the expected user ID
     * @return true if the token is valid and belongs to the user
     */
    public boolean isTokenValid(String token, String userId) {
        final String extractedUserId = extractUserId(token);
        return (extractedUserId.equals(userId)) && !isTokenExpired(token);
    }

    /**
     * Checks if a token has expired.
     *
     * @param token the JWT token
     * @return true if the token has expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token the JWT token
     * @return the claims contained in the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
