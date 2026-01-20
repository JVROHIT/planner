package com.personal.planner.domain.task;

import com.personal.planner.domain.plan.DailyPlanRepository;
import com.personal.planner.events.DomainEventPublisher;
import com.personal.planner.events.TaskCompleted;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Service for managing task intent.
 * <p>
 * "Services must not call each other directly for meaning."
 * "All cross-domain effects flow through events."
 * </p>
 * <p>
 * Constraints:
 * - MUST NEVER record completion timestamps internally. (Use events).
 * - MUST NEVER update a task associated with a closed DailyPlan.
 * </p>
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final DomainEventPublisher eventPublisher;

    public TaskService(TaskRepository taskRepository,
            DailyPlanRepository dailyPlanRepository,
            DomainEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.dailyPlanRepository = dailyPlanRepository;
        this.eventPublisher = eventPublisher;
    }

    public Task createTask(Task task) {
        // // validate input
        return taskRepository.save(task);
    }

    public Task updateTask(Task task) {
        // // validate input
        return taskRepository.save(task);
    }

    public void deleteTask(String taskId) {
        taskRepository.deleteById(taskId);
    }

    /**
     * Records the factual execution of a task on a specific day.
     * <p>
     * Logic:
     * 1. Load DailyPlan for user/date
     * 2. Find entry for taskId
     * 3. Mark completed
     * 4. Save DailyPlan
     * 5. Publish TaskCompleted fact
     * </p>
     * <p>
     * Constraints:
     * - Must not compute completion ratios.
     * - Must not touch streaks or update goals.
     * - Must not close the day.
     * - Must NOT modify the Task entity itself (Execution truth belongs to
     * DailyPlan/Events).
     * </p>
     */
    public void completeTask(String taskId, LocalDate date, String userId) {
        dailyPlanRepository.findByUserIdAndDay(userId, date).ifPresent(plan -> {
            plan.markCompleted(taskId);
            dailyPlanRepository.save(plan);

            eventPublisher.publish(TaskCompleted.builder()
                    .taskId(taskId)
                    .userId(userId)
                    .completedAt(Instant.now())
                    .build());
        });
    }
}
