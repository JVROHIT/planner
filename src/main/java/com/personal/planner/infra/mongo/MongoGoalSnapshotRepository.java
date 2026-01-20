package com.personal.planner.infra.mongo;

import com.personal.planner.domain.analytics.GoalSnapshot;
import com.personal.planner.domain.analytics.GoalSnapshotRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the GoalSnapshotRepository for end-to-end reality
 * check.
 */
@Component
public class MongoGoalSnapshotRepository implements GoalSnapshotRepository {

    private final Map<String, GoalSnapshot> store = new ConcurrentHashMap<>();

    @Override
    public GoalSnapshot save(GoalSnapshot snapshot) {
        if (snapshot.getId() == null) {
            try {
                java.lang.reflect.Field field = GoalSnapshot.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(snapshot, java.util.UUID.randomUUID().toString());
            } catch (Exception e) {
                // ...
            }
        }
        store.put(snapshot.getId(), snapshot);
        return snapshot;
    }

    @Override
    public List<GoalSnapshot> findByGoalId(String goalId) {
        return store.values().stream()
                .filter(s -> goalId.equals(s.getGoalId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<GoalSnapshot> findByGoalIdOrderBySnapshottedAtDesc(String goalId) {
        return store.values().stream()
                .filter(s -> goalId.equals(s.getGoalId()))
                .sorted((s1, s2) -> s2.getSnapshottedAt().compareTo(s1.getSnapshottedAt()))
                .collect(Collectors.toList());
    }
}
