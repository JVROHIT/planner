package com.personal.planner.domain.common.exception;

/**
 * Base exception for all FocusFlow domain errors.
 * All custom exceptions must extend this class.
 *
 * <p>This abstract exception serves as the root of the FocusFlow exception hierarchy,
 * providing a common ancestor for all domain-specific exceptions in the application.
 * It extends {@link RuntimeException} to allow for unchecked exception handling.</p>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
public abstract class FocusFlowException extends RuntimeException {

    /**
     * Constructs a new FocusFlow exception with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    protected FocusFlowException(String message) {
        super(message);
    }

    /**
     * Constructs a new FocusFlow exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause of the exception (which is saved for later retrieval by the {@link #getCause()} method)
     */
    protected FocusFlowException(String message, Throwable cause) {
        super(message, cause);
    }
}
