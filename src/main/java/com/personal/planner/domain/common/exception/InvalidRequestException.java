package com.personal.planner.domain.common.exception;

/**
 * Exception thrown when a request fails validation.
 *
 * <p>This exception should be thrown when:</p>
 * <ul>
 *   <li>Request parameters are missing or null when required</li>
 *   <li>Request parameter values are invalid or out of acceptable range</li>
 *   <li>Request data fails business rule validation</li>
 *   <li>Request contains malformed or incorrectly formatted data</li>
 *   <li>Request violates domain constraints (e.g., date ranges, numeric limits)</li>
 *   <li>Request contains conflicting or inconsistent parameters</li>
 * </ul>
 *
 * <p>This exception is used for request-level validation failures before
 * the request is processed by domain logic. It helps distinguish between
 * invalid input and domain-level business rule violations.</p>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
public class InvalidRequestException extends FocusFlowException {

    /**
     * Constructs a new invalid request exception with the specified detail message.
     *
     * @param message the detail message explaining the validation failure
     */
    public InvalidRequestException(String message) {
        super(message);
    }

    /**
     * Constructs a new invalid request exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the validation failure
     * @param cause the cause of the exception (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
