package com.personal.planner.domain.analytics;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.EventReceipt;
import com.personal.planner.domain.common.EventReceiptRepository;
import com.personal.planner.domain.common.constants.EventConstants;
import com.personal.planner.domain.goal.GoalRepository;
import com.personal.planner.domain.goal.KeyResult;
import com.personal.planner.domain.goal.KeyResultRepository;
import com.personal.planner.events.DayClosed;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static com.personal.planner.domain.common.constants.TimeConstants.ZONE_OFFSET;

/**
 * Service responsible for capturing immutable snapshots of goal progress at day closure.
 * <p>
 * This service listens to {@link DayClosed} events and creates historical records
 * ({@link GoalSnapshot}) that represent the state of goals at a specific point in time.
 * These snapshots are used for trend analysis, historical reporting, and trajectory
 * calculations.
 * </p>
 * <p>
 * <b>Domain Constraints:</b>
 * <ul>
 *   <li>Must not modify Goals or KeyResults - only reads current state</li>
 *   <li>MUST NEVER delete or modify a snapshot once created - snapshots are immutable facts</li>
 *   <li>Idempotent - safe to receive the same event multiple times without creating duplicates</li>
 * </ul>
 * </p>
 * <p>
 * <b>Idempotency:</b>
 * Uses {@link EventReceiptRepository} to track processed events, ensuring that if the same
 * {@link DayClosed} event is received multiple times (e.g., due to service restart or retry),
 * duplicate snapshots are not created.
 * </p>
 */
@Service
public class SnapshotService {

    private final GoalRepository goalRepository;
    private final KeyResultRepository keyResultRepository;
    private final GoalSnapshotRepository snapshotRepository;
    private final EventReceiptRepository eventReceiptRepository;
    private final ClockProvider clock;

    /**
     * Constructs a new SnapshotService with required dependencies.
     *
     * @param goalRepository repository for accessing goals
     * @param keyResultRepository repository for accessing key results
     * @param snapshotRepository repository for persisting snapshots
     * @param eventReceiptRepository repository for tracking event processing receipts
     * @param clock provider for current time operations
     */
    public SnapshotService(GoalRepository goalRepository,
            KeyResultRepository keyResultRepository,
            GoalSnapshotRepository snapshotRepository,
            EventReceiptRepository eventReceiptRepository,
            ClockProvider clock) {
        this.goalRepository = goalRepository;
        this.keyResultRepository = keyResultRepository;
        this.snapshotRepository = snapshotRepository;
        this.eventReceiptRepository = eventReceiptRepository;
        this.clock = clock;
    }

    /**
     * Processes a {@link DayClosed} event by creating snapshots of all active goals
     * for the specified user.
     * <p>
     * This method:
     * <ol>
     *   <li>Checks if the event has already been processed (idempotency check)</li>
     *   <li>For each active goal belonging to the user:
     *     <ul>
     *       <li>Calculates current progress by summing all key result progress values</li>
     *       <li>Creates an immutable {@link GoalSnapshot} with the current state</li>
     *       <li>Persists the snapshot to the repository</li>
     *     </ul>
     *   </li>
     *   <li>Records an event receipt to prevent duplicate processing</li>
     * </ol>
     * </p>
     * <p>
     * If the event has already been processed (receipt exists), this method returns
     * immediately without creating any snapshots, ensuring idempotency.
     * </p>
     *
     * @param event the DayClosed event containing userId and eventId
     */
    @EventListener
    public void on(DayClosed event) {
        if (eventReceiptRepository.findByEventIdAndConsumer(event.eventId(), EventConstants.CONSUMER_SNAPSHOT).isPresent()) {
            return;
        }

        goalRepository.findByUserId(event.userId()).forEach(goal -> {
            double actualProgress = keyResultRepository.findByGoalId(goal.getId()).stream()
                    .mapToDouble(KeyResult::getProgress)
                    .sum();

            GoalSnapshot snapshot = GoalSnapshot.builder()
                    .goalId(goal.getId())
                    .progress(actualProgress)
                    .snapshottedAt(clock.now().toInstant(ZONE_OFFSET))
                    .build();

            snapshotRepository.save(snapshot);
        });

        eventReceiptRepository.save(EventReceipt.of(event.eventId(), EventConstants.CONSUMER_SNAPSHOT, clock.now().toInstant(ZONE_OFFSET)));
    }
}
