package com.personal.planner.infra.mongo;

import com.personal.planner.domain.streak.StreakRepository;
import com.personal.planner.domain.streak.StreakState;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Mongo implementation of the StreakRepository.
 */
@Component
public class MongoStreakRepository implements StreakRepository {

    @Override
    public StreakState save(StreakState streakState) {
        // // Mongo mapping goes here
        return null;
    }

    @Override
    public Optional<StreakState> findByUserId(String userId) {
        // // Mongo mapping goes here
        return Optional.empty();
    }
}
