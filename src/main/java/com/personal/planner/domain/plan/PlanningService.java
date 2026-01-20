package com.personal.planner.domain.plan;

import com.personal.planner.events.DomainEventPublisher;
import com.personal.planner.events.WeeklyPlanUpdated;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.Optional;

/**
 * Service for structural execution planning.
 * <p>
 * "Transforms intent into structure."
 * </p>
 * <p>
 * Constraints:
 * - MUST NEVER compute analytics, streaks, or success metrics.
 * - MUST NEVER modify DailyPlans where {@code closed == true}.
 * - MUST ONLY project WeeklyPlan intent onto the structural horizon.
 * </p>
 */
@Service
public class PlanningService {

    private final WeeklyPlanRepository weeklyPlanRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final DomainEventPublisher eventPublisher;

    public PlanningService(WeeklyPlanRepository weeklyPlanRepository,
            DailyPlanRepository dailyPlanRepository,
            DomainEventPublisher eventPublisher) {
        this.weeklyPlanRepository = weeklyPlanRepository;
        this.dailyPlanRepository = dailyPlanRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Projects WeeklyPlan changes onto relevant DailyPlans.
     * Listens to {@link WeeklyPlanUpdated}.
     */
    @EventListener
    public void onWeeklyPlanUpdated(WeeklyPlanUpdated event) {
        // Logic to reconcile structural consistency after intent changes
    }

    /**
     * Materializes a structural DailyPlan for a specific date and user.
     * <p>
     * Logic:
     * - If DailyPlan for date does not exist:
     * - Read user's WeeklyPlan
     * - Create new DailyPlan (copied from week grid)
     * - Set closed = false
     * - Save
     * </p>
     * <p>
     * Constraint: NEVER compute streaks or completion ratios here.
     * </p>
     */
    public void materializeDay(LocalDate date, String userId) {
        Optional<DailyPlan> existingDay = dailyPlanRepository.findByUserIdAndDay(userId, date);

        if (existingDay.isEmpty()) {
            int weekNumber = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            int year = date.get(IsoFields.WEEK_BASED_YEAR);

            weeklyPlanRepository.findByUserAndWeek(userId, weekNumber, year).ifPresent(weeklyPlan -> {
                DailyPlan newDay = DailyPlan.builder()
                        .userId(userId)
                        .day(date)
                        .closed(false)
                        .build();
                // // Copy entries from weeklyPlan grid would happen here
                dailyPlanRepository.save(newDay);
            });
        }
    }

    /**
     * Reconciliation: Ensures structural integrity for the entire week.
     * <p>
     * Logic:
     * - For each day in the week:
     * - Ensure a DailyPlan exists (unless the day is already closed).
     * </p>
     * <p>
     * Constraint: MUST NEVER touch closed days.
     * </p>
     */
    public void reconcileWeeklyPlan(WeeklyPlan weeklyPlan) {
        // // Logic would iterate through the week's days and call materializeDay for
        // each
        // // while ensuring no mutation of closed historical facts.
        // // NEVER emit analytics or update goals here.
        eventPublisher.publish(WeeklyPlanUpdated.builder()
                .planId(weeklyPlan.getId())
                .userId(weeklyPlan.getUserId())
                .build());
    }

    public void injectTaskIntoOpenDay(String taskId, LocalDate date) {
        // Logic to add specific intent to an open day
    }
}
