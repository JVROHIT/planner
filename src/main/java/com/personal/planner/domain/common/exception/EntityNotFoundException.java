package com.personal.planner.domain.common.exception;

/**
 * Exception thrown when an entity cannot be found by its identifier.
 *
 * <p>This exception is thrown when attempting to retrieve an entity from the data store
 * using its unique identifier, but no matching entity exists. It serves as the base
 * exception for all entity-specific not found exceptions.</p>
 *
 * <p>The exception automatically formats the error message to include the entity type
 * and identifier for better debugging and logging.</p>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
public class EntityNotFoundException extends FocusFlowException {

    /**
     * Constructs a new entity not found exception with the specified entity type and identifier.
     * The error message will be formatted as: "Entity {type} with id {id} not found"
     *
     * @param entityType the type of entity that was not found (e.g., "Task", "Goal")
     * @param entityId the identifier of the entity that was not found
     */
    public EntityNotFoundException(String entityType, String entityId) {
        super(String.format("Entity %s with id %s not found", entityType, entityId));
    }

    /**
     * Constructs a new entity not found exception with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new entity not found exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause of the exception (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
