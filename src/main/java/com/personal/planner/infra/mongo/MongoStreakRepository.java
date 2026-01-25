package com.personal.planner.infra.mongo;

import com.personal.planner.domain.streak.StreakRepository;
import com.personal.planner.domain.streak.StreakState;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the StreakRepository.
 *
 * <p>Stores streak state entities in a ConcurrentHashMap keyed by userId.
 * This is a temporary implementation for development/testing - production
 * should use actual MongoDB collections.</p>
 *
 * <p>Streak state is a derived interpretation of DayClosed events.
 * It is never edited directly by users - only updated by the streak service
 * in response to domain events.</p>
 */
@Component
public class MongoStreakRepository implements StreakRepository {

    private final Map<String, StreakState> store = new ConcurrentHashMap<>();

    @Override
    public StreakState save(StreakState state) {
        if (state.getId() == null) {
            // // Reflection-based ID injection
            try {
                java.lang.reflect.Field field = StreakState.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(state, java.util.UUID.randomUUID().toString());
            } catch (Exception e) {
                // ...
            }
        }
        store.put(state.getUserId(), state);
        return state;
    }

    @Override
    public Optional<StreakState> findByUserId(String userId) {
        return Optional.ofNullable(store.get(userId));
    }
}
