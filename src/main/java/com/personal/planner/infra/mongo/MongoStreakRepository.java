package com.personal.planner.infra.mongo;

import com.personal.planner.domain.streak.StreakRepository;
import com.personal.planner.domain.streak.StreakState;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the StreakRepository for end-to-end reality
 * check.
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
