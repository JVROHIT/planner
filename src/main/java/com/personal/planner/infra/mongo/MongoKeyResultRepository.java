package com.personal.planner.infra.mongo;

import com.personal.planner.domain.goal.KeyResult;
import com.personal.planner.domain.goal.KeyResultRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the KeyResultRepository.
 *
 * <p>Stores key result entities in a ConcurrentHashMap for thread-safe access.
 * This is a temporary implementation for development/testing - production
 * should use actual MongoDB collections.</p>
 *
 * <p>Key results are measurable outcomes that contribute to goals.
 * They can be of different types: ACCUMULATIVE, HABIT, or MILESTONE.</p>
 *
 * <p>Custom queries:
 * <ul>
 *   <li>findByGoalId: Returns all key results for a specific goal</li>
 * </ul>
 * </p>
 */
@Component
public class MongoKeyResultRepository implements KeyResultRepository {

    private final Map<String, KeyResult> store = new ConcurrentHashMap<>();

    @Override
    public KeyResult save(KeyResult keyResult) {
        if (keyResult.getId() == null) {
            // // Reflection to set ID since it might be protected/private
            try {
                java.lang.reflect.Field field = KeyResult.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(keyResult, java.util.UUID.randomUUID().toString());
            } catch (Exception e) {
                // ... fallback if field name is different or other error
            }
        }
        store.put(keyResult.getId(), keyResult);
        return keyResult;
    }

    @Override
    public Optional<KeyResult> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }

    @Override
    public List<KeyResult> findByGoalId(String goalId) {
        return store.values().stream()
                .filter(kr -> goalId.equals(kr.getGoalId()))
                .collect(Collectors.toList());
    }
}
