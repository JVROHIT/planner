package com.personal.planner.events;

import lombok.*;
import java.time.Instant;

/**
 * Domain event representing the creation of a new task.
 *
 * <p><strong>When emitted:</strong></p>
 * <p>This event is emitted immediately after a new task is successfully
 * created and persisted in the TaskService. It represents the immutable
 * fact that a task with specific properties now exists in the system.</p>
 *
 * <p><strong>Event fields:</strong></p>
 * <ul>
 *   <li><strong>taskId:</strong> The unique identifier of the newly created task</li>
 *   <li><strong>userId:</strong> The ID of the user who created the task</li>
 *   <li><strong>createdAt:</strong> The exact moment the task was created</li>
 * </ul>
 *
 * <p><strong>What downstream systems should assume:</strong></p>
 * <ul>
 *   <li>A task with the given taskId now exists</li>
 *   <li>The task belongs to the specified user</li>
 *   <li>The task was created at the specified timestamp</li>
 *   <li>The task is available for planning and execution</li>
 * </ul>
 *
 * <p><strong>What downstream systems must NOT infer:</strong></p>
 * <ul>
 *   <li>The task's title, description, or other properties (not in event)</li>
 *   <li>Whether the task is linked to goals or key results</li>
 *   <li>The user's productivity or task creation patterns</li>
 *   <li>Any business rules about task limits or validation</li>
 * </ul>
 *
 * <p><strong>Typical consumers:</strong></p>
 * <ul>
 *   <li>Analytics service (for task creation metrics)</li>
 *   <li>Audit service (for user activity logging)</li>
 *   <li>Notification service (for task creation alerts)</li>
 * </ul>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class TaskCreated implements DomainEvent {
    /** Unique identifier for this event instance. */
    private String id;
    
    /** The unique identifier of the newly created task. */
    private String taskId;
    
    /** The ID of the user who created the task. */
    private String userId;
    
    /** The exact moment when the task was created. */
    private Instant createdAt;

    /**
     * Returns the timestamp when this task creation occurred.
     *
     * @return the instant when the task was created
     */
    @Override
    public Instant occurredAt() {
        return createdAt;
    }

    /**
     * Returns the unique identifier for this event instance.
     *
     * @return the event ID
     */
    @Override
    public String eventId() {
        return id;
    }

    /**
     * Returns the ID of the user who created the task.
     *
     * @return the user ID
     */
    @Override
    public String userId() {
        return userId;
    }
}
