package com.personal.planner.domain.common.exception;

/**
 * Exception thrown when an authenticated user lacks permission to perform an action.
 *
 * <p>This exception should be thrown when:</p>
 * <ul>
 *   <li>A user attempts to access or modify a resource they do not own</li>
 *   <li>A user lacks the required role or permission for an operation</li>
 *   <li>A user attempts to perform an administrative action without admin privileges</li>
 *   <li>A user attempts to access resources outside their authorized scope</li>
 *   <li>Ownership validation fails for a user-specific resource</li>
 * </ul>
 *
 * <p>This exception indicates that the user has been successfully authenticated
 * but is not authorized to perform the requested action. It is distinct from
 * {@link AuthenticationException}, which indicates that the user's identity
 * could not be verified.</p>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
public class AuthorizationException extends FocusFlowException {

    /**
     * Constructs a new authorization exception with the specified detail message.
     *
     * @param message the detail message explaining the reason for the authorization failure
     */
    public AuthorizationException(String message) {
        super(message);
    }

    /**
     * Constructs a new authorization exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the authorization failure
     * @param cause the cause of the exception (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
