package com.personal.planner.infra.mongo;

import com.personal.planner.domain.plan.DailyPlan;
import com.personal.planner.domain.plan.DailyPlanRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Mongo implementation of the DailyPlanRepository.
 */
@Component
public class MongoDailyPlanRepository implements DailyPlanRepository {

    @Override
    public DailyPlan save(DailyPlan dailyPlan) {
        // // Mongo mapping goes here
        return dailyPlan;
    }

    @Override
    public Optional<DailyPlan> findByUserIdAndDay(String userId, LocalDate day) {
        // // Mongo mapping goes here
        return Optional.empty();
    }

    @Override
    public Optional<DailyPlan> findById(String id) {
        // // Mongo mapping goes here
        return Optional.empty();
    }
}
