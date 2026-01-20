package com.personal.planner.infra.mongo;

import com.personal.planner.domain.goal.Goal;
import com.personal.planner.domain.goal.GoalRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/**
 * Mongo implementation of the GoalRepository.
 */
@Component
public class MongoGoalRepository implements GoalRepository {

    @Override
    public Goal save(Goal goal) {
        // // Mongo mapping goes here
        return null;
    }

    @Override
    public Optional<Goal> findById(String id) {
        // // Mongo mapping goes here
        return Optional.empty();
    }

    @Override
    public List<Goal> findByUserId(String userId) {
        // // Mongo mapping goes here
        return List.of();
    }

    @Override
    public void deleteById(String id) {
        // // Mongo mapping goes here
    }
}
