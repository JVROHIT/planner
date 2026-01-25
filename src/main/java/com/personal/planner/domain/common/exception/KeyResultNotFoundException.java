package com.personal.planner.domain.common.exception;

/**
 * Exception thrown when a KeyResult entity cannot be found by its identifier.
 *
 * <p>This exception should be thrown when:</p>
 * <ul>
 *   <li>Attempting to retrieve a key result by ID that does not exist</li>
 *   <li>Attempting to update or delete a non-existent key result</li>
 *   <li>Attempting to update progress on a non-existent key result</li>
 *   <li>Attempting to access a key result that has been deleted</li>
 * </ul>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
public class KeyResultNotFoundException extends EntityNotFoundException {

    /**
     * Constructs a new key result not found exception with the specified entity type and key result identifier.
     *
     * @param entityType the type of entity (e.g., "KeyResult")
     * @param entityId the identifier of the key result that was not found
     */
    public KeyResultNotFoundException(String entityType, String entityId) {
        super(entityType, entityId);
    }

    /**
     * Constructs a new key result not found exception with the specified key result identifier.
     *
     * @param keyResultId the identifier of the key result that was not found
     */
    public KeyResultNotFoundException(String keyResultId) {
        super("KeyResult", keyResultId);
    }
}
