package com.personal.planner.domain.plan;

import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Persistence boundary for DailyPlan.
 * <p>
 * Constraints:
 * - Must not encode business rules.
 * </p>
 */
@Repository
public interface DailyPlanRepository {
    DailyPlan save(DailyPlan dailyPlan);

    Optional<DailyPlan> findByUserIdAndDay(String userId, LocalDate day);

    Optional<DailyPlan> findById(String id);
}
