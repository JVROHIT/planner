package com.personal.planner.domain.plan;

import com.personal.planner.events.DayClosed;
import com.personal.planner.events.DomainEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Service to orchestrate the closure of the daily cycle.
 * <p>
 * "Defines the boundary between ‘now’ and ‘history’."
 * </p>
 * <p>
 * Constraints:
 * - MUST NOT compute streaks, stats, or goal progress.
 * - MUST NEVER perform any post-closure updates on a DailyPlan.
 * - MUST ONLY be responsible for freezing structural truth.
 * </p>
 */
@Service
public class DayCloseService {

    private final DailyPlanRepository dailyPlanRepository;
    private final DomainEventPublisher eventPublisher;

    public DayCloseService(DailyPlanRepository dailyPlanRepository,
            DomainEventPublisher eventPublisher) {
        this.dailyPlanRepository = dailyPlanRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Heartbeat method to finalize the current execution cycle.
     * <p>
     * Logic:
     * - Iterates through all users (placeholder for iteration).
     * - Loads today's DailyPlan.
     * - If exists and not closed:
     * - Mark closed = true.
     * - Save.
     * - Publish DayClosed event.
     * </p>
     * <p>
     * Constraint: NEVER compute success/failure or update streaks here.
     * </p>
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void runDayCloseProcess() {
        LocalDate today = LocalDate.now();

        // // Placeholder for user iteration: for each userId in system...
        // String userId = "...";
        // dailyPlanRepository.findByUserIdAndDay(userId,
        // today).ifPresent(this::closePlanAndPublish);
    }

    /**
     * Internal helper to finalize a DailyPlan fact.
     */
    private void closePlanAndPublish(DailyPlan plan) {
        if (!plan.isClosed()) {
            plan.setClosed(true);
            dailyPlanRepository.save(plan);

            eventPublisher.publish(DayClosed.builder()
                    .userId(plan.getUserId())
                    .day(plan.getDay())
                    .closedAt(Instant.now())
                    .build());
        }
    }

    public void closeDay() {
        // Method for explicit closure trigger
    }
}
