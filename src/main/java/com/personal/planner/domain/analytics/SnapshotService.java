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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
     *       <li>Calculates actual progress from key results (weighted average)</li>
     *       <li>Calculates expected progress from the goal's date range</li>
     *       <li>Creates an immutable {@link GoalSnapshot} with actual/expected values</li>
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
            var keyResults = keyResultRepository.findByGoalId(goal.getId());
            double totalWeight = keyResults.stream()
                    .mapToDouble(KeyResult::getWeight)
                    .sum();
            double weightedSum = keyResults.stream()
                    .mapToDouble(kr -> kr.getProgress() * kr.getWeight())
                    .sum();
            double actualProgress = totalWeight > 0 ? (weightedSum / totalWeight) : 0;

            double expectedProgress = calculateExpectedProgress(event.getDay(), goal.getStartDate(), goal.getEndDate());

            GoalSnapshot snapshot = GoalSnapshot.builder()
                    .goalId(goal.getId())
                    .date(event.getDay())
                    .actual(actualProgress)
                    .expected(expectedProgress)
                    .build();

            snapshotRepository.save(snapshot);
        });

        eventReceiptRepository.save(EventReceipt.of(event.eventId(), EventConstants.CONSUMER_SNAPSHOT, clock.now().toInstant(ZONE_OFFSET)));
    }

    private double calculateExpectedProgress(LocalDate day, LocalDate startDate, LocalDate endDate) {
        if (day == null || startDate == null || endDate == null) {
            return 0;
        }
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (totalDays <= 0) {
            return 0;
        }
        long elapsedDays = ChronoUnit.DAYS.between(startDate, day);
        double expected = (double) elapsedDays / totalDays;
        if (expected < 0) {
            return 0;
        }
        return Math.min(expected, 1);
    }
}
