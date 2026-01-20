package com.personal.planner.domain.plan;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

/**
 * Read model for UI and dashboards.
 * <p>
 * Constraints:
 * - Must never modify domain state.
 * - Must not infer meaning beyond what already exists.
 * - Read from repositories only.
 * </p>
 */
@Service
public class DailyPlanQueryService {

    private final DailyPlanRepository dailyPlanRepository;

    public DailyPlanQueryService(DailyPlanRepository dailyPlanRepository) {
        this.dailyPlanRepository = dailyPlanRepository;
    }

    /**
     * Retrieves today's execution truth for a user.
     */
    public DailyPlan getToday(String userId) {
        return dailyPlanRepository.findByUserIdAndDay(userId, LocalDate.now()).orElse(null);
    }

    /**
     * Retrieves a list of DailyPlans for a specific week horizon.
     */
    public List<DailyPlan> getWeek(String userId, LocalDate weekStart) {
        // // Logic to fetch DailyPlans for the 7 days starting from weekStart
        return List.of();
    }
}
