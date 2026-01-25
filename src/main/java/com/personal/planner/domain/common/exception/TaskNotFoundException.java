package com.personal.planner.domain.common.exception;

/**
 * Exception thrown when a Task entity cannot be found by its identifier.
 *
 * <p>This exception should be thrown when:</p>
 * <ul>
 *   <li>Attempting to retrieve a task by ID that does not exist</li>
 *   <li>Attempting to update or delete a non-existent task</li>
 *   <li>Attempting to access a task that has been deleted</li>
 * </ul>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
public class TaskNotFoundException extends EntityNotFoundException {

    /**
     * Constructs a new task not found exception with the specified entity type and task identifier.
     *
     * @param entityType the type of entity (e.g., "Task")
     * @param entityId the identifier of the task that was not found
     */
    public TaskNotFoundException(String entityType, String entityId) {
        super(entityType, entityId);
    }

    /**
     * Constructs a new task not found exception with the specified task identifier.
     *
     * @param taskId the identifier of the task that was not found
     */
    public TaskNotFoundException(String taskId) {
        super("Task", taskId);
    }
}
