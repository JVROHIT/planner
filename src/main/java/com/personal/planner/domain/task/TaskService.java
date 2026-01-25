package com.personal.planner.domain.task;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.exception.DomainViolationException;
import com.personal.planner.domain.plan.DailyPlanRepository;
import com.personal.planner.events.DomainEventPublisher;
import com.personal.planner.events.TaskCompleted;
import com.personal.planner.events.TaskCreated;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static com.personal.planner.domain.common.constants.TimeConstants.ZONE_OFFSET;

/**
 * Service for managing task intent.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final DomainEventPublisher eventPublisher;
    private final ClockProvider clock;

    public TaskService(TaskRepository taskRepository,
            DailyPlanRepository dailyPlanRepository,
            DomainEventPublisher eventPublisher,
            ClockProvider clock) {
        this.taskRepository = taskRepository;
        this.dailyPlanRepository = dailyPlanRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public Task createTask(Task task) {
        Task saved = taskRepository.save(task);
        eventPublisher.publish(TaskCreated.builder()
                .id(UUID.randomUUID().toString())
                .taskId(saved.getId())
                .userId(saved.getUserId())
                .createdAt(clock.now().toInstant(ZONE_OFFSET))
                .build());
        return saved;
    }

    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(String taskId) {
        taskRepository.deleteById(taskId);
    }

    public void completeTask(String taskId, LocalDate date, String userId) {
        dailyPlanRepository.findByUserIdAndDay(userId, date)
                .map(plan -> {
                    plan.markCompleted(taskId);
                    dailyPlanRepository.save(plan);

                    Task task = taskRepository.findById(taskId).orElse(null);
                    String goalId = task != null ? task.getGoalId() : null;
                    String keyResultId = task != null ? task.getKeyResultId() : null;

                    eventPublisher.publish(TaskCompleted.builder()
                            .id(UUID.randomUUID().toString())
                            .taskId(taskId)
                            .userId(userId)
                            .completedAt(clock.now().toInstant(ZONE_OFFSET))
                            .goalId(goalId)
                            .keyResultId(keyResultId)
                            .build());
                    return plan;
                })
                .orElseThrow(() -> new DomainViolationException(
                        "Actionable truth cannot exist without structure. No open DailyPlan found for date: " + date));
    }

    public void missTask(String taskId, LocalDate date, String userId) {
        dailyPlanRepository.findByUserIdAndDay(userId, date)
                .ifPresent(plan -> {
                    plan.markMissed(taskId);
                    dailyPlanRepository.save(plan);
                    // No event required by spec, but truth is recorded.
                });
    }
}
