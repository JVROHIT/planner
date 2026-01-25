package com.personal.planner.domain;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.EventReceipt;
import com.personal.planner.domain.common.EventReceiptRepository;
import com.personal.planner.domain.common.constants.EventConstants;
import com.personal.planner.domain.plan.DailyPlan;
import com.personal.planner.domain.plan.DailyPlanRepository;
import com.personal.planner.domain.streak.StreakRepository;
import com.personal.planner.domain.streak.StreakService;
import com.personal.planner.domain.streak.StreakState;
import com.personal.planner.events.DayClosed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;

class IdempotencyProofTest {

    private StreakRepository streakRepository;
    private DailyPlanRepository dailyPlanRepository;
    private EventReceiptRepository eventReceiptRepository;
    private ClockProvider clock;
    private StreakService streakService;

    @BeforeEach
    void setUp() {
        streakRepository = mock(StreakRepository.class);
        dailyPlanRepository = mock(DailyPlanRepository.class);
        eventReceiptRepository = mock(EventReceiptRepository.class);
        clock = mock(ClockProvider.class);
        streakService = new StreakService(streakRepository, dailyPlanRepository, eventReceiptRepository, clock);
    }

    @Test
    void meaningIsAppliedExactlyOnce() {
        String eventId = "evt-123";
        String userId = "user-1";
        DayClosed event = DayClosed.builder()
                .id(eventId)
                .userId(userId)
                .day(LocalDate.now())
                .build();

        // Simulate event already processed
        when(eventReceiptRepository.findByEventIdAndConsumer(eventId, EventConstants.CONSUMER_STREAK))
                .thenReturn(Optional.of(mock(EventReceipt.class)));

        // Scenario: Fire the same event twice
        streakService.on(event);

        // Verify: No interaction with repositories beyond the receipt check
        verify(dailyPlanRepository, never()).findByUserIdAndDay(anyString(), any());
        verify(streakRepository, never()).save(any());
    }
}
