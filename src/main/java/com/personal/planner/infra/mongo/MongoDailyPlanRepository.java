package com.personal.planner.infra.mongo;

import com.personal.planner.domain.plan.DailyPlan;
import com.personal.planner.domain.plan.DailyPlanRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the DailyPlanRepository for end-to-end reality
 * check.
 */
@Component
public class MongoDailyPlanRepository implements DailyPlanRepository {

    private final Map<String, DailyPlan> store = new ConcurrentHashMap<>();

    @Override
    public DailyPlan save(DailyPlan dailyPlan) {
        if (dailyPlan.getId() == null) {
            // // Reflection-based ID injection for in-memory shim
            try {
                java.lang.reflect.Field field = DailyPlan.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(dailyPlan, java.util.UUID.randomUUID().toString());
            } catch (Exception e) {
                // ...
            }
        }
        store.put(dailyPlan.getId(), dailyPlan);
        return dailyPlan;
    }

    @Override
    public Optional<DailyPlan> findByUserIdAndDay(String userId, LocalDate day) {
        return store.values().stream()
                .filter(p -> p.getUserId().equals(userId) && p.getDay().equals(day))
                .findFirst();
    }

    @Override
    public Optional<DailyPlan> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
}
