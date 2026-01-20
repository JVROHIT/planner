package com.personal.planner.domain.plan;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.events.DayClosed;
import com.personal.planner.events.DomainEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Service to orchestrate the closure of the daily cycle.
 */
@Service
public class DayCloseService {

    private final DailyPlanRepository dailyPlanRepository;
    private final DomainEventPublisher eventPublisher;
    private final ClockProvider clock;

    public DayCloseService(DailyPlanRepository dailyPlanRepository,
            DomainEventPublisher eventPublisher,
            ClockProvider clock) {
        this.dailyPlanRepository = dailyPlanRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void runDayCloseProcess() {
        // ...
    }

    public void closeDayExplicit(String userId, LocalDate date) {
        dailyPlanRepository.findByUserIdAndDay(userId, date).ifPresent(this::closePlanAndPublish);
    }

    public void closePlanAndPublish(DailyPlan plan) {
        if (!plan.isClosed()) {
            plan.close();
            dailyPlanRepository.save(plan);

            eventPublisher.publish(DayClosed.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(plan.getUserId())
                    .day(plan.getDay())
                    .closedAt(clock.now())
                    .build());
        }
    }
}
