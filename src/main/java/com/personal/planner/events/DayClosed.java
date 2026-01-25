package com.personal.planner.events;

import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Domain event representing the closing of a daily planning cycle.
 *
 * <p><strong>When emitted:</strong></p>
 * <p>This event is emitted when a user explicitly closes their daily plan
 * through the DayCloseService. It represents the immutable fact that a
 * specific day's planning cycle has been completed and finalized.</p>
 *
 * <p><strong>CRITICAL IMMUTABILITY IMPLICATIONS:</strong></p>
 * <p>Once a DayClosed event is emitted, the corresponding DailyPlan becomes
 * immutable. No further modifications to tasks, completion status, or
 * planning data are allowed for that day. This ensures data integrity
 * for analytics, streaks, and historical reporting.</p>
 *
 * <p><strong>Event fields:</strong></p>
 * <ul>
 *   <li><strong>day:</strong> The specific date (LocalDate) that was closed</li>
 *   <li><strong>userId:</strong> The ID of the user who closed the day</li>
 *   <li><strong>closedAt:</strong> The exact moment the day was closed</li>
 * </ul>
 *
 * <p><strong>What downstream systems should assume:</strong></p>
 * <ul>
 *   <li>The specified day's planning cycle is complete and final</li>
 *   <li>All task completion data for that day is now immutable</li>
 *   <li>The day's statistics (completion ratio, task count) are final</li>
 *   <li>The day can be used for streak calculations and analytics</li>
 * </ul>
 *
 * <p><strong>What downstream systems must NOT infer:</strong></p>
 * <ul>
 *   <li>How many tasks were completed (query the DailyPlan directly)</li>
 *   <li>The user's satisfaction or productivity level</li>
 *   <li>Whether the day was "successful" (that's a business interpretation)</li>
 *   <li>Any specific completion percentage or metrics</li>
 *   <li>The user's planning habits or patterns</li>
 * </ul>
 *
 * <p><strong>Typical consumers:</strong></p>
 * <ul>
 *   <li>Streak service (to update current/longest streak counters)</li>
 *   <li>Analytics service (to create goal snapshots and trend data)</li>
 *   <li>Audit service (for day closure activity logging)</li>
 *   <li>Notification service (for streak achievement alerts)</li>
 * </ul>
 *
 * <p><strong>Timezone considerations:</strong></p>
 * <p>The 'day' field represents a date in the user's configured timezone
 * (Asia/Kolkata). The 'closedAt' instant represents the exact UTC moment
 * when the closure occurred.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class DayClosed implements DomainEvent {
    /** Unique identifier for this event instance. */
    private String id;
    
    /** The specific date that was closed (in user's timezone). */
    private LocalDate day;
    
    /** The ID of the user who closed the day. */
    private String userId;
    
    /** The exact moment when the day was closed. */
    private Instant closedAt;

    /**
     * Returns the timestamp when this day closure occurred.
     *
     * @return the instant when the day was closed
     */
    @Override
    public Instant occurredAt() {
        return closedAt;
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
     * Returns the ID of the user who closed the day.
     *
     * @return the user ID
     */
    @Override
    public String userId() {
        return userId;
    }
}
