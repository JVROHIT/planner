package com.personal.planner.domain.common.exception;

/**
 * Exception thrown when a User entity cannot be found by its identifier.
 *
 * <p>This exception should be thrown when:</p>
 * <ul>
 *   <li>Attempting to retrieve a user by ID that does not exist</li>
 *   <li>Attempting to retrieve a user by username or email that does not exist</li>
 *   <li>Attempting to update or delete a non-existent user</li>
 *   <li>Attempting to authenticate a user that does not exist</li>
 *   <li>Attempting to access a user that has been deleted or deactivated</li>
 * </ul>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
public class UserNotFoundException extends EntityNotFoundException {

    /**
     * Constructs a new user not found exception with the specified entity type and user identifier.
     *
     * @param entityType the type of entity (e.g., "User")
     * @param entityId the identifier of the user that was not found
     */
    public UserNotFoundException(String entityType, String entityId) {
        super(entityType, entityId);
    }

    /**
     * Constructs a new user not found exception with the specified user identifier.
     *
     * @param userId the identifier of the user that was not found
     */
    public UserNotFoundException(String userId) {
        super("User", userId);
    }
}
