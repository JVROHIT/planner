package com.personal.planner.events;

import lombok.*;
import java.time.Instant;

/**
 * Domain event representing an update to a weekly plan.
 *
 * <p><strong>When emitted:</strong></p>
 * <p>This event is emitted whenever a user modifies their weekly plan
 * through the PlanningService. This includes adding tasks to specific days,
 * removing tasks, or rearranging the weekly schedule. It represents the
 * immutable fact that the plan's structure was changed.</p>
 *
 * <p><strong>Event fields:</strong></p>
 * <ul>
 *   <li><strong>planId:</strong> The unique identifier of the updated weekly plan</li>
 *   <li><strong>userId:</strong> The ID of the user who updated the plan</li>
 *   <li><strong>updatedAt:</strong> The exact moment the plan was modified</li>
 * </ul>
 *
 * <p><strong>What downstream systems should assume:</strong></p>
 * <ul>
 *   <li>The specified weekly plan has been modified</li>
 *   <li>The plan's task-to-day mappings may have changed</li>
 *   <li>The update occurred at the specified timestamp</li>
 *   <li>The plan is now in a new state compared to before</li>
 * </ul>
 *
 * <p><strong>What downstream systems must NOT infer:</strong></p>
 * <ul>
 *   <li>What specific changes were made (tasks added/removed/moved)</li>
 *   <li>The plan's current content or structure</li>
 *   <li>Whether the update was an improvement or optimization</li>
 *   <li>The user's planning strategy or decision-making process</li>
 *   <li>Any validation rules about plan completeness or balance</li>
 * </ul>
 *
 * <p><strong>Typical consumers:</strong></p>
 * <ul>
 *   <li>Analytics service (for planning activity metrics)</li>
 *   <li>Audit service (for plan modification history)</li>
 *   <li>Cache invalidation services (to refresh plan-based caches)</li>
 * </ul>
 *
 * <p><strong>Note on frequency:</strong></p>
 * <p>This event may be emitted frequently during active planning sessions.
 * Consumers should be designed to handle high-frequency updates efficiently
 * and consider debouncing strategies if needed.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class WeeklyPlanUpdated implements DomainEvent {
    /** Unique identifier for this event instance. */
    private String id;
    
    /** The unique identifier of the updated weekly plan. */
    private String planId;
    
    /** The ID of the user who updated the plan. */
    private String userId;
    
    /** The exact moment when the plan was updated. */
    private Instant updatedAt;

    /**
     * Returns the timestamp when this plan update occurred.
     *
     * @return the instant when the plan was updated
     */
    @Override
    public Instant occurredAt() {
        return updatedAt;
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
     * Returns the ID of the user who updated the plan.
     *
     * @return the user ID
     */
    @Override
    public String userId() {
        return userId;
    }
}
