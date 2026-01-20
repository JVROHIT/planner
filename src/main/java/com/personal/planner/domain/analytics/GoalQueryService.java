package com.personal.planner.domain.analytics;

import com.personal.planner.domain.goal.Goal;
import com.personal.planner.domain.goal.GoalRepository;
import com.personal.planner.domain.goal.KeyResult;
import com.personal.planner.domain.goal.KeyResultRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Read model for UI and dashboards.
 * <p>
 * Constraints:
 * - Must never modify domain state.
 * - Must not infer meaning beyond what already exists.
 * - Read from repositories only.
 * </p>
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

    /**
     * Retrieves all active directional goals for a user.
     */
    public List<Goal> getActiveGoals(String userId) {
        return goalRepository.findByUserId(userId);
    }

    /**
     * Retrieves evaluative units for a specific goal.
     */
    public List<KeyResult> getKeyResults(String goalId) {
        return keyResultRepository.findByGoalId(goalId);
    }

    /**
     * Retrieves historical progress facts for a specific goal.
     */
    public List<GoalSnapshot> getSnapshots(String goalId, int days) {
        return snapshotRepository.findByGoalId(goalId);
    }
}
