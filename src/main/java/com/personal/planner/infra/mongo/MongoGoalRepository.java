package com.personal.planner.infra.mongo;

import com.personal.planner.domain.goal.Goal;
import com.personal.planner.domain.goal.GoalRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the GoalRepository for end-to-end reality check.
 */
@Component
public class MongoGoalRepository implements GoalRepository {

    private final Map<String, Goal> store = new ConcurrentHashMap<>();

    @Override
    public Goal save(Goal goal) {
        if (goal.getId() == null) {
            goal.setId(java.util.UUID.randomUUID().toString());
        }
        store.put(goal.getId(), goal);
        return goal;
    }

    @Override
    public List<Goal> findByUserId(String userId) {
        return store.values().stream()
                .filter(g -> g.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Goal> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
