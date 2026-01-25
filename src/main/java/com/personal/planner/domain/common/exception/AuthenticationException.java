package com.personal.planner.domain.common.exception;

/**
 * Exception thrown when authentication fails.
 *
 * <p>This exception should be thrown when:</p>
 * <ul>
 *   <li>Login credentials (username/password) are invalid</li>
 *   <li>JWT token is invalid, expired, or malformed</li>
 *   <li>JWT token signature verification fails</li>
 *   <li>Required authentication information is missing</li>
 *   <li>User account is locked or disabled</li>
 *   <li>Authentication token has been revoked or blacklisted</li>
 * </ul>
 *
 * <p>This exception indicates that the user's identity could not be verified.
 * It is distinct from {@link AuthorizationException}, which indicates that
 * the authenticated user lacks permission to perform an action.</p>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
public class AuthenticationException extends FocusFlowException {

    /**
     * Constructs a new authentication exception with the specified detail message.
     *
     * @param message the detail message explaining the reason for the authentication failure
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructs a new authentication exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the authentication failure
     * @param cause the cause of the exception (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
