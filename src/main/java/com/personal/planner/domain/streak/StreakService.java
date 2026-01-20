package com.personal.planner.domain.streak;

import com.personal.planner.domain.plan.DailyPlan;
import com.personal.planner.domain.plan.DailyPlanRepository;
import com.personal.planner.events.DayClosed;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service for calculating behavioral consistency.
 * <p>
 * "All cross-domain effects flow through events."
 * "This preserves temporal truth and decoupling."
 * </p>
 * <p>
 * Constraints:
 * - MUST NEVER allow manual override of streak values.
 * - MUST ONLY derive state from the sequence of closed days.
 * - Meaning flows only from events.
 * </p>
 */
@Service
public class StreakService {

    private final StreakRepository streakRepository;
    private final DailyPlanRepository dailyPlanRepository;

    public StreakService(StreakRepository streakRepository, DailyPlanRepository dailyPlanRepository) {
        this.streakRepository = streakRepository;
        this.dailyPlanRepository = dailyPlanRepository;
    }

    /**
     * Updates the user's streak based on the factual close of a daily cycle.
     * Listens to {@link DayClosed}.
     * 
     * Logic:
     * 1. Load the closed DailyPlan.
     * 2. If all tasks were completed and there was at least one task: increment
     * streak.
     * 3. Else: reset streak.
     * 4. Persist StreakState.
     */
    @EventListener
    public void on(DayClosed event) {
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
        });
    }
}
