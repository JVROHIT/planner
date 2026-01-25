package com.personal.planner.domain.common.exception;

/**
 * Exception thrown when an error occurs during event processing.
 *
 * <p>This exception should be thrown when:</p>
 * <ul>
 *   <li>Event handler execution fails due to an unexpected error</li>
 *   <li>Event payload is malformed or cannot be deserialized</li>
 *   <li>Event publishing to message broker fails</li>
 *   <li>Event listener encounters an unrecoverable error</li>
 *   <li>Event processing violates business rules or invariants</li>
 *   <li>Required event data is missing or invalid</li>
 *   <li>Event processing timeout occurs</li>
 * </ul>
 *
 * <p>This exception is used to capture failures in the event-driven architecture,
 * including both domain event publication and consumption. It helps distinguish
 * between event processing issues and other domain exceptions.</p>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
public class EventProcessingException extends FocusFlowException {

    /**
     * Constructs a new event processing exception with the specified detail message.
     *
     * @param message the detail message explaining the event processing failure
     */
    public EventProcessingException(String message) {
        super(message);
    }

    /**
     * Constructs a new event processing exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the event processing failure
     * @param cause the cause of the exception (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public EventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
