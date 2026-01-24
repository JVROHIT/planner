package com.personal.planner.domain.analytics;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.EventReceipt;
import com.personal.planner.domain.common.EventReceiptRepository;
import com.personal.planner.domain.goal.GoalRepository;
import com.personal.planner.domain.goal.KeyResult;
import com.personal.planner.domain.goal.KeyResultRepository;
import com.personal.planner.events.DayClosed;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service for freezing progress into historical facts.
 * <p>
 * Constraints:
 * - Must not modify Goals or KeyResults.
 * - MUST NEVER delete or modify a snapshot once created.
 * - Safe to receive the same event more than once. (Idempotent).
 * </p>
 */
@Service
public class SnapshotService {

    private final GoalRepository goalRepository;
    private final KeyResultRepository keyResultRepository;
    private final GoalSnapshotRepository snapshotRepository;
    private final EventReceiptRepository eventReceiptRepository;
    private final ClockProvider clock;

    private static final String CONSUMER_NAME = "SNAPSHOT";

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
     * Captures goal state into a GoalSnapshot upon day closure.
     * Listens to {@link DayClosed}.
     */
    @EventListener
    public void on(DayClosed event) {
        if (eventReceiptRepository.findByEventIdAndConsumer(event.eventId(), CONSUMER_NAME).isPresent()) {
            return;
        }

        goalRepository.findByUserId(event.userId()).forEach(goal -> {
            double actualProgress = keyResultRepository.findByGoalId(goal.getId()).stream()
                    .mapToDouble(KeyResult::getProgress)
                    .sum();

            GoalSnapshot snapshot = GoalSnapshot.builder()
                    .goalId(goal.getId())
                    .progress(actualProgress)
                    .snapshottedAt(clock.now())
                    .build();

            snapshotRepository.save(snapshot);
        });

        eventReceiptRepository.save(EventReceipt.of(event.eventId(), CONSUMER_NAME, clock.now()));
    }
}
