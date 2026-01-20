package com.personal.planner.domain.analytics;

import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Persistence boundary for GoalSnapshot.
 * <p>
 * Constraints:
 * - Must not encode business rules.
 * </p>
 */
@Repository
public interface GoalSnapshotRepository {
    GoalSnapshot save(GoalSnapshot snapshot);

    List<GoalSnapshot> findByGoalId(String goalId);
}
