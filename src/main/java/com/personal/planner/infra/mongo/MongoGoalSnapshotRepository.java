package com.personal.planner.infra.mongo;

import com.personal.planner.domain.analytics.GoalSnapshot;
import com.personal.planner.domain.analytics.GoalSnapshotRepository;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Mongo implementation of the GoalSnapshotRepository.
 */
@Component
public class MongoGoalSnapshotRepository implements GoalSnapshotRepository {

    @Override
    public GoalSnapshot save(GoalSnapshot snapshot) {
        // // Mongo mapping goes here
        return null;
    }

    @Override
    public List<GoalSnapshot> findByGoalId(String goalId) {
        // // Mongo mapping goes here
        return List.of();
    }
}
