package com.personal.planner.domain.analytics;

import com.personal.planner.domain.goal.GoalRepository;
import com.personal.planner.domain.goal.KeyResult;
import com.personal.planner.domain.goal.KeyResultRepository;
import com.personal.planner.events.DayClosed;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import java.time.Instant;

/**
 * Service for freezing progress into historical facts.
 * <p>
 * "All cross-domain effects flow through events."
 * "This preserves temporal truth and decoupling."
 * </p>
 * <p>
 * Constraints:
 * - MUST NEVER delete or modify a snapshot once created.
 * - MUST ONLY be triggered by the closure of a structural cycle.
 * - Meaning flows only from events.
 * </p>
 */
@Service
public class SnapshotService {

    private final GoalRepository goalRepository;
    private final KeyResultRepository keyResultRepository;
    private final GoalSnapshotRepository snapshotRepository;

    public SnapshotService(GoalRepository goalRepository,
            KeyResultRepository keyResultRepository,
            GoalSnapshotRepository snapshotRepository) {
        this.goalRepository = goalRepository;
        this.keyResultRepository = keyResultRepository;
        this.snapshotRepository = snapshotRepository;
    }

    /**
     * Captures goal state into a GoalSnapshot upon day closure.
     * Listens to {@link DayClosed}.
     * 
     * Logic:
     * 1. For each active Goal for the user.
     * 2. Compute actual progress (sum of KeyResult progress).
     * 3. Compute expected progress (linear progress by date placeholder).
     * 4. Persist GoalSnapshot.
     */
    @EventListener
    public void on(DayClosed event) {
        goalRepository.findByUserId(event.userId()).forEach(goal -> {
            double actualProgress = keyResultRepository.findByGoalId(goal.getId()).stream()
                    .mapToDouble(KeyResult::getCurrentProgress)
                    .sum();

            // // Expected progress calculation placeholder
            // // Based on goal start/end dates and current date
            double expectedProgress = 0.0;

            GoalSnapshot snapshot = GoalSnapshot.builder()
                    .goalId(goal.getId())
                    .progress(actualProgress)
                    .snapshottedAt(Instant.now())
                    .build();

            snapshotRepository.save(snapshot);
        });
    }
}
