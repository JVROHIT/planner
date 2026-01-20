package com.personal.planner.infra.mongo;

import com.personal.planner.domain.plan.WeeklyPlan;
import com.personal.planner.domain.plan.WeeklyPlanRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Mongo implementation of the WeeklyPlanRepository.
 */
@Component
public class MongoWeeklyPlanRepository implements WeeklyPlanRepository {

    @Override
    public WeeklyPlan save(WeeklyPlan weeklyPlan) {
        // // Mongo mapping goes here
        return null;
    }

    @Override
    public Optional<WeeklyPlan> findByUserAndWeek(String userId, int weekNumber, int year) {
        // // Mongo mapping goes here
        return Optional.empty();
    }

    @Override
    public Optional<WeeklyPlan> findById(String id) {
        // // Mongo mapping goes here
        return Optional.empty();
    }
}
