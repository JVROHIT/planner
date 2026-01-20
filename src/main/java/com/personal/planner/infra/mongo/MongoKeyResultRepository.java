package com.personal.planner.infra.mongo;

import com.personal.planner.domain.goal.KeyResult;
import com.personal.planner.domain.goal.KeyResultRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/**
 * Mongo implementation of the KeyResultRepository.
 */
@Component
public class MongoKeyResultRepository implements KeyResultRepository {

    @Override
    public KeyResult save(KeyResult keyResult) {
        // // Mongo mapping goes here
        return null;
    }

    @Override
    public Optional<KeyResult> findById(String id) {
        // // Mongo mapping goes here
        return Optional.empty();
    }

    @Override
    public List<KeyResult> findByGoalId(String goalId) {
        // // Mongo mapping goes here
        return List.of();
    }
}
