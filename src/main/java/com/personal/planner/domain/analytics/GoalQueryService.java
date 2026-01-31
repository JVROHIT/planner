package com.personal.planner.domain.analytics;

import com.personal.planner.domain.goal.Goal;
import com.personal.planner.domain.goal.GoalRepository;
import com.personal.planner.domain.goal.KeyResult;
import com.personal.planner.domain.goal.KeyResultRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Read-only query service for retrieving goal-related data for UI and dashboards.
 * <p>
 * This service provides a clean read model interface for accessing goals, key results,
 * and historical snapshots. It follows the CQRS pattern by separating read operations
 * from write operations, ensuring that query methods do not modify domain state.
 * </p>
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Retrieve active goals for a user</li>
 *   <li>Retrieve key results for a specific goal</li>
 *   <li>Retrieve historical snapshots for trend analysis and reporting</li>
 * </ul>
 * </p>
 * <p>
 * <b>Design Principles:</b>
 * <ul>
 *   <li>Read-only operations - no state modification</li>
 *   <li>Optimized for UI and dashboard consumption</li>
 *   <li>Returns data in formats suitable for presentation</li>
 * </ul>
 * </p>
 */
@Service
public class GoalQueryService {

    private final GoalRepository goalRepository;
    private final KeyResultRepository keyResultRepository;
    private final GoalSnapshotRepository snapshotRepository;

    /**
     * Constructs a new GoalQueryService with required repositories.
     *
     * @param goalRepository repository for accessing goals
     * @param keyResultRepository repository for accessing key results
     * @param snapshotRepository repository for accessing historical snapshots
     */
    public GoalQueryService(GoalRepository goalRepository,
            KeyResultRepository keyResultRepository,
            GoalSnapshotRepository snapshotRepository) {
        this.goalRepository = goalRepository;
        this.keyResultRepository = keyResultRepository;
        this.snapshotRepository = snapshotRepository;
    }

    /**
     * Retrieves all active goals for a specific user.
     * <p>
     * Active goals are goals that are currently in progress and have not been
     * completed or archived. This method is typically used to populate user
     * dashboards and goal management interfaces.
     * </p>
     *
     * @param userId the unique identifier of the user whose goals should be retrieved
     * @return a list of active goals for the user, ordered as returned by the repository.
     *         Returns an empty list if the user has no active goals.
     */
    public List<Goal> getActiveGoals(String userId) {
        return goalRepository.findByUserId(userId).stream()
                .filter(goal -> goal.getStatus() == null || goal.getStatus() == Goal.Status.ACTIVE)
                .toList();
    }

    /**
     * Retrieves all key results associated with a specific goal.
     * <p>
     * Key results are measurable outcomes that contribute to goal achievement.
     * This method is used to display the breakdown of progress for a goal,
     * showing individual key result contributions.
     * </p>
     *
     * @param goalId the unique identifier of the goal whose key results should be retrieved
     * @return a list of key results for the specified goal.
     *         Returns an empty list if the goal has no key results or does not exist.
     */
    public List<KeyResult> getKeyResults(String goalId) {
        return keyResultRepository.findByGoalId(goalId);
    }

    /**
     * Retrieves historical snapshots for a goal, limited to a specified number of days.
     * <p>
     * Snapshots are returned in descending order by snapshot date (most recent first),
     * which is the natural ordering for trend analysis and charting. The results are
     * limited to the most recent N days as specified by the days parameter.
     * </p>
     * <p>
     * This method is typically used for:
     * <ul>
     *   <li>Displaying progress charts and graphs</li>
     *   <li>Trend analysis calculations</li>
     *   <li>Historical progress reporting</li>
     * </ul>
     * </p>
     *
     * @param goalId the unique identifier of the goal whose snapshots should be retrieved
     * @param days the maximum number of days of snapshots to retrieve (most recent N days)
     * @return a list of goal snapshots ordered by snapshot date descending (most recent first),
     *         limited to the specified number of days. Returns an empty list if no snapshots
     *         exist for the goal or if the goal does not exist.
     */
    public List<GoalSnapshot> getSnapshots(String goalId, int days) {
        return snapshotRepository.findByGoalIdOrderByDateDesc(goalId).stream()
                .limit(days)
                .toList();
    }
}
