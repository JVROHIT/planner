package com.personal.planner.domain.analytics;

import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Persistence boundary for GoalSnapshot.
 */
@Repository
public interface GoalSnapshotRepository {
    GoalSnapshot save(GoalSnapshot snapshot);

    List<GoalSnapshot> findByGoalId(String goalId);

    List<GoalSnapshot> findByGoalIdOrderByDateDesc(String goalId);
}
