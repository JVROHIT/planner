package com.personal.planner.domain.common.exception;

/**
 * Exception thrown when an architectural invariant or domain law is violated.
 *
 * <p>This exception should be thrown when:</p>
 * <ul>
 *   <li>Attempting to mutate a closed or finalized entity (e.g., closed DailyPlan)</li>
 *   <li>Attempting to rewrite or modify historical facts or immutable data</li>
 *   <li>Detecting circular dependencies between domain entities</li>
 *   <li>Violating aggregate boundaries or domain constraints</li>
 *   <li>Attempting operations that break domain invariants</li>
 *   <li>Violating business rules that are fundamental to the domain model</li>
 * </ul>
 *
 * <p>This exception represents violations of core domain rules that should never
 * occur in a correctly functioning system. It indicates that the application has
 * entered an invalid state according to the business domain's fundamental laws.</p>
 *
 * <p>Unlike {@link InvalidRequestException} which handles validation of user input,
 * this exception is for violations of deeper domain invariants that should be
 * prevented by the application's architecture and logic.</p>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
public class DomainViolationException extends FocusFlowException {

    /**
     * Constructs a new domain violation exception with the specified detail message.
     *
     * @param message the detail message explaining which domain invariant was violated
     */
    public DomainViolationException(String message) {
        super(message);
    }

    /**
     * Constructs a new domain violation exception with the specified detail message and cause.
     *
     * @param message the detail message explaining which domain invariant was violated
     * @param cause the cause of the exception (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public DomainViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
