package com.personal.planner.domain.analytics;

import com.personal.planner.domain.goal.Goal;
import com.personal.planner.domain.goal.GoalRepository;
import com.personal.planner.domain.goal.KeyResult;
import com.personal.planner.domain.goal.KeyResultRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Read model for UI and dashboards.
 */
@Service
public class GoalQueryService {

    private final GoalRepository goalRepository;
    private final KeyResultRepository keyResultRepository;
    private final GoalSnapshotRepository snapshotRepository;

    public GoalQueryService(GoalRepository goalRepository,
            KeyResultRepository keyResultRepository,
            GoalSnapshotRepository snapshotRepository) {
        this.goalRepository = goalRepository;
        this.keyResultRepository = keyResultRepository;
        this.snapshotRepository = snapshotRepository;
    }

    public List<Goal> getActiveGoals(String userId) {
        return goalRepository.findByUserId(userId);
    }

    public List<KeyResult> getKeyResults(String goalId) {
        return keyResultRepository.findByGoalId(goalId);
    }

    public List<GoalSnapshot> getSnapshots(String goalId, int days) {
        return snapshotRepository.findByGoalIdOrderBySnapshottedAtDesc(goalId).stream()
                .limit(days)
                .toList();
    }
}
