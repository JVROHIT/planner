package com.personal.planner.events;

import lombok.*;
import java.time.Instant;

/**
 * Domain event representing the completion of a task.
 *
 * <p><strong>When emitted:</strong></p>
 * <p>This event is emitted when a task is marked as completed through the
 * TaskService. It represents the immutable fact that a specific task
 * transitioned from incomplete to complete at a specific moment.</p>
 *
 * <p><strong>Event fields:</strong></p>
 * <ul>
 *   <li><strong>taskId:</strong> The unique identifier of the completed task</li>
 *   <li><strong>userId:</strong> The ID of the user who completed the task</li>
 *   <li><strong>completedAt:</strong> The exact moment the task was marked complete</li>
 *   <li><strong>goalId:</strong> The ID of the goal this task contributes to (optional)</li>
 *   <li><strong>keyResultId:</strong> The ID of the key result this task advances (optional)</li>
 * </ul>
 *
 * <p><strong>What downstream systems should assume:</strong></p>
 * <ul>
 *   <li>The specified task is now in completed state</li>
 *   <li>The completion happened at the specified timestamp</li>
 *   <li>If goalId/keyResultId are present, this completion contributes to those objectives</li>
 *   <li>The task is no longer available for execution</li>
 * </ul>
 *
 * <p><strong>What downstream systems must NOT infer:</strong></p>
 * <ul>
 *   <li>The task's original content, priority, or other properties</li>
 *   <li>How long the task took to complete</li>
 *   <li>The user's productivity level or work patterns</li>
 *   <li>Whether this completion affects goal progress (that's calculated separately)</li>
 *   <li>Any business rules about task completion validation</li>
 * </ul>
 *
 * <p><strong>Typical consumers:</strong></p>
 * <ul>
 *   <li>Goal service (to update key result progress)</li>
 *   <li>Analytics service (for completion metrics and trends)</li>
 *   <li>Streak service (for daily completion tracking)</li>
 *   <li>Audit service (for user activity logging)</li>
 * </ul>
 *
 * <p><strong>Optional fields:</strong></p>
 * <p>goalId and keyResultId may be null if the task is not linked to any objectives.
 * Consumers should handle null values gracefully.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class TaskCompleted implements DomainEvent {
    /** Unique identifier for this event instance. */
    private String id;
    
    /** The unique identifier of the completed task. */
    private String taskId;
    
    /** The ID of the user who completed the task. */
    private String userId;
    
    /** The exact moment when the task was marked complete. */
    private Instant completedAt;
    
    /** The ID of the goal this task contributes to (may be null). */
    private String goalId;
    
    /** The ID of the key result this task advances (may be null). */
    private String keyResultId;

    /**
     * Returns the timestamp when this task completion occurred.
     *
     * @return the instant when the task was completed
     */
    @Override
    public Instant occurredAt() {
        return completedAt;
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
     * Returns the ID of the user who completed the task.
     *
     * @return the user ID
     */
    @Override
    public String userId() {
        return userId;
    }
}
