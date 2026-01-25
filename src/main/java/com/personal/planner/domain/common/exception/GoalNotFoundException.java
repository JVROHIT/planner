package com.personal.planner.domain.common.exception;

/**
 * Exception thrown when a Goal entity cannot be found by its identifier.
 *
 * <p>This exception should be thrown when:</p>
 * <ul>
 *   <li>Attempting to retrieve a goal by ID that does not exist</li>
 *   <li>Attempting to update or delete a non-existent goal</li>
 *   <li>Attempting to add key results to a non-existent goal</li>
 *   <li>Attempting to access a goal that has been deleted</li>
 * </ul>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
public class GoalNotFoundException extends EntityNotFoundException {

    /**
     * Constructs a new goal not found exception with the specified entity type and goal identifier.
     *
     * @param entityType the type of entity (e.g., "Goal")
     * @param entityId the identifier of the goal that was not found
     */
    public GoalNotFoundException(String entityType, String entityId) {
        super(entityType, entityId);
    }

    /**
     * Constructs a new goal not found exception with the specified goal identifier.
     *
     * @param goalId the identifier of the goal that was not found
     */
    public GoalNotFoundException(String goalId) {
        super("Goal", goalId);
    }
}
