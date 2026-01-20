package com.personal.planner.domain.plan;

import com.personal.planner.domain.common.ClockProvider;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Read model for UI and dashboards.
 */
@Service
public class DailyPlanQueryService {

    private final DailyPlanRepository dailyPlanRepository;
    private final PlanningService planningService;
    private final ClockProvider clock;

    public DailyPlanQueryService(DailyPlanRepository dailyPlanRepository,
            PlanningService planningService,
            ClockProvider clock) {
        this.dailyPlanRepository = dailyPlanRepository;
        this.planningService = planningService;
        this.clock = clock;
    }

    /**
     * Retrieves today's execution truth. Auto-materializes if missing.
     */
    public DailyPlan getToday(String userId) {
        LocalDate today = clock.today();
        return dailyPlanRepository.findByUserIdAndDay(userId, today)
                .orElseGet(() -> {
                    planningService.materializeDay(today, userId);
                    return dailyPlanRepository.findByUserIdAndDay(userId, today).orElse(null);
                });
    }

    public DailyPlan getDay(String userId, LocalDate date) {
        return dailyPlanRepository.findByUserIdAndDay(userId, date).orElse(null);
    }

    public List<DailyPlan> getWeek(String userId, LocalDate weekStart) {
        List<DailyPlan> plans = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            dailyPlanRepository.findByUserIdAndDay(userId, weekStart.plusDays(i))
                    .ifPresent(plans::add);
        }
        return plans;
    }
}
