package com.personal.planner.domain.goal;

import com.personal.planner.domain.plan.DailyPlan;
import com.personal.planner.domain.plan.DailyPlanRepository;
import com.personal.planner.domain.task.Task;
import com.personal.planner.domain.task.TaskRepository;
import com.personal.planner.events.DayClosed;
import com.personal.planner.events.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Evaluator for Habit-based Key Results.
 * <p>
 * "Habits are measured by showing up."
 * Progress increments only once per day, on DayClosed, if at least one
 * contributing task was completed.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class HabitKREvaluator {

    private final DailyPlanRepository dailyPlanRepository;
    private final TaskRepository taskRepository;

    public boolean supports(KeyResult kr) {
        return kr.getType() == KeyResult.Type.HABIT;
    }

    public void handle(KeyResult kr, DomainEvent event) {
        if (event instanceof DayClosed) {
            DayClosed dayClosed = (DayClosed) event;

            // 1. Find the DailyPlan for that day
            Optional<DailyPlan> planOpt = dailyPlanRepository.findByUserIdAndDay(dayClosed.userId(),
                    dayClosed.getDay());

            if (planOpt.isPresent()) {
                DailyPlan plan = planOpt.get();

                // 2. Check if any COMPLETED task in that plan is linked to this Habit KR
                boolean consistencyMet = plan.getTasks().stream()
                        .filter(DailyPlan.TaskExecution::isCompleted)
                        .anyMatch(execution -> {
                            Optional<Task> task = taskRepository.findById(execution.getTaskId());
                            return task.isPresent() && kr.getId().equals(task.get().getKeyResultId());
                        });

                // 3. Increment if consistency logic is met
                if (consistencyMet) {
                    kr.applyProgress(1.0);
                }
            }
        }
    }
}
