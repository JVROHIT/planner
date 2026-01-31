package com.personal.planner.infra.mongo;

import com.personal.planner.domain.plan.WeeklyPlan;
import com.personal.planner.domain.plan.WeeklyPlanRepository;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the WeeklyPlanRepository.
 *
 * <p>Stores weekly plan entities in a ConcurrentHashMap for thread-safe access.
 * This is a temporary implementation for development/testing - production
 * should use actual MongoDB collections.</p>
 *
 * <p>Weekly plans represent the user's editable intent grid for a week,
 * mapping days to scheduled task IDs.</p>
 */
@Component
public class MongoWeeklyPlanRepository implements WeeklyPlanRepository {

    private final Map<String, WeeklyPlan> store = new ConcurrentHashMap<>();

    @Override
    public WeeklyPlan save(WeeklyPlan weeklyPlan) {
        if (weeklyPlan.getId() == null) {
            weeklyPlan.setId(java.util.UUID.randomUUID().toString());
        }
        store.put(weeklyPlan.getId(), weeklyPlan);
        return weeklyPlan;
    }

    @Override
    public Optional<WeeklyPlan> findByUserAndWeekStart(String userId, java.time.LocalDate weekStart) {
        return store.values().stream()
                .filter(p -> p.getUserId().equals(userId) && weekStart.equals(p.getWeekStart()))
                .findFirst();
    }

    @Override
    public Optional<WeeklyPlan> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
}
