package com.personal.planner.domain.common.constants;

/**
 * Event consumer identifier constants for FocusFlow event processing.
 * <p>
 * These constants are used to identify event consumers in the event receipt system,
 * ensuring idempotent event processing. Each consumer records a receipt when processing
 * an event, preventing duplicate processing of the same event.
 * </p>
 * <p>
 * Event flow example:
 * 1. TaskCompletedEvent is published
 * 2. GoalService processes with CONSUMER_GOAL and creates receipt
 * 3. StreakService processes with CONSUMER_STREAK and creates receipt
 * 4. If service restarts, receipts prevent reprocessing
 * </p>
 */
public final class EventConstants {

    private EventConstants() {
        // Prevent instantiation
    }

    /**
     * Consumer identifier for Goal Service.
     * Used when goal service processes events to update goal progress.
     */
    public static final String CONSUMER_GOAL = "GOAL";

    /**
     * Consumer identifier for Streak Service.
     * Used when streak service processes events to update user streaks.
     */
    public static final String CONSUMER_STREAK = "STREAK";

    /**
     * Consumer identifier for Snapshot Service.
     * Used when snapshot service processes events to capture daily snapshots.
     */
    public static final String CONSUMER_SNAPSHOT = "SNAPSHOT";

    /**
     * Consumer identifier for Audit Service.
     * Used when audit service processes events to log audit trails.
     */
    public static final String CONSUMER_AUDIT = "AUDIT";
}
