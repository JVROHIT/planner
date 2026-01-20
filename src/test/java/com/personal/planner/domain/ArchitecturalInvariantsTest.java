package com.personal.planner.domain;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.DomainViolationException;
import com.personal.planner.domain.plan.DailyPlan;
import com.personal.planner.domain.plan.DailyPlanRepository;
import com.personal.planner.domain.plan.PlanningService;
import com.personal.planner.domain.plan.WeeklyPlan;
import com.personal.planner.domain.plan.WeeklyPlanRepository;
import com.personal.planner.events.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Proof of Philosophical Inforcement.
 * "You may change the future without lying about the past."
 */
class ArchitecturalInvariantsTest {

    private DailyPlanRepository dailyPlanRepository;
    private WeeklyPlanRepository weeklyPlanRepository;
    private DomainEventPublisher eventPublisher;
    private ClockProvider clock;
    private PlanningService planningService;

    @BeforeEach
    void setUp() {
        dailyPlanRepository = mock(DailyPlanRepository.class);
        weeklyPlanRepository = mock(WeeklyPlanRepository.class);
        eventPublisher = mock(DomainEventPublisher.class);
        clock = mock(ClockProvider.class);
        planningService = new PlanningService(weeklyPlanRepository, dailyPlanRepository, eventPublisher, clock);
    }

    @Test
    void cannotMutateClosedDay() {
        DailyPlan closedDay = DailyPlan.builder()
                .day(LocalDate.now())
                .closed(true)
                .build();

        // Scenario 1: Try to mark a task completed on a closed day
        assertThrows(DomainViolationException.class, () -> {
            closedDay.markCompleted("task-1");
        });
    }

    @Test
    void cannotRewriteHistoryViaMaterialization() {
        String userId = "user-1";
        LocalDate date = LocalDate.now();
        DailyPlan closedDay = DailyPlan.builder()
                .userId(userId)
                .day(date)
                .closed(true)
                .build();

        when(dailyPlanRepository.findByUserIdAndDay(userId, date)).thenReturn(Optional.of(closedDay));
        when(clock.today()).thenReturn(date);

        // Scenario 2: Try to materialize a day that already exists (and is closed)
        planningService.materializeDay(date, userId);

        // Verify: No new save was called
        verify(dailyPlanRepository, never()).save(any(DailyPlan.class));
    }

    @Test
    void cannotMaterializeStructureInThePast() {
        String userId = "user-1";
        LocalDate pastDate = LocalDate.now().minusDays(1);

        when(clock.today()).thenReturn(LocalDate.now());
        when(dailyPlanRepository.findByUserIdAndDay(userId, pastDate)).thenReturn(Optional.empty());

        // Scenario 3: Try to materialize a non-existent day in the past
        assertThrows(DomainViolationException.class, () -> {
            planningService.materializeDay(pastDate, userId);
        });
    }
}
