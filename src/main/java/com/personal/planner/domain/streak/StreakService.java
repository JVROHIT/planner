package com.personal.planner.domain.streak;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.EventReceipt;
import com.personal.planner.domain.common.EventReceiptRepository;
import com.personal.planner.domain.plan.DailyPlan;
import com.personal.planner.domain.plan.DailyPlanRepository;
import com.personal.planner.events.DayClosed;
import com.personal.planner.events.UserCreated;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service for calculating behavioral consistency.
 * <p>
 * Constraints:
 * - Must not read WeeklyPlan or Tasks directly.
 * - MUST ONLY derive state from the sequence of closed days.
 * - Safe to receive the same event more than once. (Idempotent).
 * </p>
 */
@Service
public class StreakService {

    private final StreakRepository streakRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final EventReceiptRepository eventReceiptRepository;
    private final ClockProvider clock;

    private static final String CONSUMER_NAME = "STREAK";

    public StreakService(StreakRepository streakRepository,
            DailyPlanRepository dailyPlanRepository,
            EventReceiptRepository eventReceiptRepository,
            ClockProvider clock) {
        this.streakRepository = streakRepository;
        this.dailyPlanRepository = dailyPlanRepository;
        this.eventReceiptRepository = eventReceiptRepository;
        this.clock = clock;
    }

    /**
     * Updates the user's streak based on the factual close of a daily cycle.
     * Listens to {@link DayClosed}.
     * 
     * Idempotency Guard:
     * - Check if event already processed by this consumer.
     */
    @EventListener
    public void on(DayClosed event) {
        if (eventReceiptRepository.findByEventIdAndConsumer(event.eventId(), CONSUMER_NAME).isPresent()) {
            return;
        }

        dailyPlanRepository.findByUserIdAndDay(event.userId(), event.getDay()).ifPresent(plan -> {
            StreakState state = streakRepository.findByUserId(event.userId())
                    .orElseGet(() -> StreakState.builder().userId(event.userId()).currentStreak(0).build());

            long totalTasks = plan.getTasks().size();
            long completedTasks = plan.getTasks().stream().filter(DailyPlan.TaskExecution::isCompleted).count();

            if (totalTasks > 0 && completedTasks == totalTasks) {
                state.setCurrentStreak(state.getCurrentStreak() + 1);
            } else {
                state.setCurrentStreak(0);
            }

            streakRepository.save(state);
            eventReceiptRepository.save(EventReceipt.of(event.eventId(), CONSUMER_NAME, clock.now()));
        });
    }

    @EventListener
    public void on(UserCreated event) {
        if (eventReceiptRepository.findByEventIdAndConsumer(event.eventId(), CONSUMER_NAME).isPresent()) {
            return;
        }

        StreakState initialState = StreakState.builder()
                .userId(event.userId())
                .currentStreak(0)
                .build();
        streakRepository.save(initialState);
        eventReceiptRepository.save(EventReceipt.of(event.eventId(), CONSUMER_NAME, clock.now()));
    }
}
