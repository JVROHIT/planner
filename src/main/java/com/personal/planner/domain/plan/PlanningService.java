package com.personal.planner.domain.plan;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.DomainViolationException;
import com.personal.planner.events.DomainEventPublisher;
import com.personal.planner.events.WeeklyPlanUpdated;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for structural execution planning.
 */
@Service
public class PlanningService {

    private final WeeklyPlanRepository weeklyPlanRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final DomainEventPublisher eventPublisher;
    private final ClockProvider clock;

    public PlanningService(WeeklyPlanRepository weeklyPlanRepository,
            DailyPlanRepository dailyPlanRepository,
            DomainEventPublisher eventPublisher,
            ClockProvider clock) {
        this.weeklyPlanRepository = weeklyPlanRepository;
        this.dailyPlanRepository = dailyPlanRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public WeeklyPlan createWeeklyPlan(WeeklyPlan plan) {
        return weeklyPlanRepository.save(plan);
    }

    public Optional<WeeklyPlan> getWeeklyPlan(String userId, int weekNumber, int year) {
        return weeklyPlanRepository.findByUserAndWeek(userId, weekNumber, year);
    }

    @EventListener
    public void onWeeklyPlanUpdated(WeeklyPlanUpdated event) {
        // ...
    }

    public void materializeDay(LocalDate date, String userId) {
        Optional<DailyPlan> existingDay = dailyPlanRepository.findByUserIdAndDay(userId, date);

        if (existingDay.isEmpty()) {
            if (date.isBefore(clock.today())) {
                throw new DomainViolationException("Cannot materialize structure in the past: " + date);
            }

            int weekNumber = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            int year = date.get(IsoFields.WEEK_BASED_YEAR);

            weeklyPlanRepository.findByUserAndWeek(userId, weekNumber, year).ifPresent(weeklyPlan -> {
                DailyPlan newDay = DailyPlan.builder()
                        .userId(userId)
                        .day(date)
                        .closed(false)
                        .tasks(weeklyPlan.getTasksFor(date.getDayOfWeek()).stream()
                                .map(taskId -> DailyPlan.TaskExecution.builder()
                                        .taskId(taskId)
                                        .completed(false)
                                        .build())
                                .collect(Collectors.toList()))
                        .build();
                dailyPlanRepository.save(newDay);
            });
        }
    }

    public void reconcileWeeklyPlan(WeeklyPlan weeklyPlan) {
        eventPublisher.publish(WeeklyPlanUpdated.builder()
                .id(UUID.randomUUID().toString())
                .planId(weeklyPlan.getId())
                .userId(weeklyPlan.getUserId())
                .updatedAt(clock.now())
                .build());
    }
}
