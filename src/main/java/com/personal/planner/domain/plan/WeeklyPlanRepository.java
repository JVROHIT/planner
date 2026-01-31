package com.personal.planner.domain.plan;

import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Persistence boundary for WeeklyPlan.
 * <p>
 * Constraints:
 * - Must not encode business rules.
 * </p>
 */
@Repository
public interface WeeklyPlanRepository {
    WeeklyPlan save(WeeklyPlan weeklyPlan);

    Optional<WeeklyPlan> findByUserAndWeekStart(String userId, java.time.LocalDate weekStart);

    Optional<WeeklyPlan> findById(String id);
}
