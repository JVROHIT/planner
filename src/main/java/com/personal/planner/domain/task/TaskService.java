package com.personal.planner.domain.task;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.exception.AuthorizationException;
import com.personal.planner.domain.common.exception.DomainViolationException;
import com.personal.planner.domain.common.exception.TaskNotFoundException;
import com.personal.planner.domain.common.util.LogUtil;
import com.personal.planner.domain.plan.DailyPlanRepository;
import com.personal.planner.events.DomainEventPublisher;
import com.personal.planner.events.TaskCompleted;
import com.personal.planner.events.TaskCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.UUID;

import static com.personal.planner.domain.common.constants.TimeConstants.ZONE_OFFSET;

/**
 * Service for managing task intent and lifecycle operations.
 * <p>
 * This service is responsible for:
 * <ul>
 *   <li>Creating new tasks and publishing TaskCreated events</li>
 *   <li>Updating existing tasks with ownership validation</li>
 *   <li>Deleting tasks with ownership validation</li>
 *   <li>Completing tasks within daily plans and publishing TaskCompleted events</li>
 *   <li>Marking tasks as missed within daily plans</li>
 * </ul>
 * <p>
 * All operations enforce ownership validation to ensure users can only modify
 * tasks they own. The service maintains task intent as mutable records until
 * they are finalized by action through daily plan operations.
 *
 * @author FocusFlow Team
 * @since 1.0
 */
@Service
public class TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final DomainEventPublisher eventPublisher;
    private final ClockProvider clock;

    /**
     * Constructs a new TaskService with the required dependencies.
     *
     * @param taskRepository the repository for task persistence operations
     * @param dailyPlanRepository the repository for daily plan operations
     * @param eventPublisher the publisher for domain events
     * @param clock the clock provider for time-based operations
     */
    public TaskService(TaskRepository taskRepository,
            DailyPlanRepository dailyPlanRepository,
            DomainEventPublisher eventPublisher,
            ClockProvider clock) {
        this.taskRepository = taskRepository;
        this.dailyPlanRepository = dailyPlanRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    /**
     * Creates a new task and publishes a TaskCreated event.
     * <p>
     * The task is persisted and a domain event is published to notify
     * other parts of the system about the task creation.
     *
     * @param task the task to create (must not be null)
     * @return the saved task with its generated identifier
     */
    public Task createTask(Task task) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[TaskService] Creating task for user: {}", task.getUserId());
        }

        Task saved = taskRepository.save(task);
        eventPublisher.publish(TaskCreated.builder()
                .id(UUID.randomUUID().toString())
                .taskId(saved.getId())
                .userId(saved.getUserId())
                .createdAt(clock.now().toInstant(ZONE_OFFSET))
                .build());

        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[TaskService] Task created successfully: taskId={}, userId={}", 
                    saved.getId(), saved.getUserId());
        }

        return saved;
    }

    /**
     * Updates an existing task after validating ownership.
     * <p>
     * This method ensures that only the task owner can update the task.
     * The task must exist and belong to the specified user.
     *
     * @param task the task to update (must not be null, must have id and userId set)
     * @return the updated task
     * @throws TaskNotFoundException if the task with the given ID does not exist
     * @throws AuthorizationException if the user does not own the task
     */
    public Task updateTask(Task task) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[TaskService] Updating task: taskId={}, userId={}", 
                    task.getId(), task.getUserId());
        }

        Task existing = taskRepository.findById(task.getId())
                .orElseThrow(() -> new TaskNotFoundException(task.getId()));

        validateOwnership(existing, task.getUserId());

        Task updated = taskRepository.save(task);

        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[TaskService] Task updated successfully: taskId={}", updated.getId());
        }

        return updated;
    }

    /**
     * Deletes a task after validating ownership.
     * <p>
     * This method ensures that only the task owner can delete the task.
     * The task must exist and belong to the specified user.
     *
     * @param taskId the identifier of the task to delete (must not be null)
     * @param userId the identifier of the user requesting the deletion (must not be null)
     * @throws TaskNotFoundException if the task with the given ID does not exist
     * @throws AuthorizationException if the user does not own the task
     */
    public void deleteTask(String taskId, String userId) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[TaskService] Deleting task: taskId={}, userId={}", taskId, userId);
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        validateOwnership(task, userId);

        taskRepository.deleteById(taskId);

        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[TaskService] Task deleted successfully: taskId={}", taskId);
        }
    }

    /**
     * Marks a task as completed within a daily plan and publishes a TaskCompleted event.
     * <p>
     * This operation requires an open daily plan for the specified date.
     * The task is marked as completed in the plan, and a domain event is published
     * with task completion details including associated goal and key result identifiers.
     *
     * @param taskId the identifier of the task to complete (must not be null)
     * @param date the date of the daily plan (must not be null)
     * @param userId the identifier of the user completing the task (must not be null)
     * @throws DomainViolationException if no open daily plan exists for the specified date
     * @throws TaskNotFoundException if the task with the given ID does not exist
     * @throws AuthorizationException if the user does not own the task
     */
    public void completeTask(String taskId, LocalDate date, String userId) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[TaskService] Completing task: taskId={}, date={}, userId={}", 
                    taskId, date, userId);
        }

        // Validate task exists and user owns it
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        validateOwnership(task, userId);

        dailyPlanRepository.findByUserIdAndDay(userId, date)
                .map(plan -> {
                    plan.markCompleted(taskId);
                    dailyPlanRepository.save(plan);

                    String goalId = task.getGoalId();
                    String keyResultId = task.getKeyResultId();

                    eventPublisher.publish(TaskCompleted.builder()
                            .id(UUID.randomUUID().toString())
                            .taskId(taskId)
                            .userId(userId)
                            .completedAt(clock.now().toInstant(ZONE_OFFSET))
                            .goalId(goalId)
                            .keyResultId(keyResultId)
                            .build());

                    if (LogUtil.isDebugEnabled()) {
                        LOG.debug("[TaskService] Task completed successfully: taskId={}, date={}", 
                                taskId, date);
                    }

                    return plan;
                })
                .orElseThrow(() -> new DomainViolationException(
                        "Actionable truth cannot exist without structure. No open DailyPlan found for date: " + date));
    }

    /**
     * Marks a task as missed within a daily plan.
     * <p>
     * This operation updates the daily plan to record that the task was missed.
     * If no daily plan exists for the specified date, the operation is silently ignored.
     * No domain event is published for missed tasks as per specification.
     *
     * @param taskId the identifier of the task to mark as missed (must not be null)
     * @param date the date of the daily plan (must not be null)
     * @param userId the identifier of the user (must not be null)
     * @throws TaskNotFoundException if the task with the given ID does not exist
     * @throws AuthorizationException if the user does not own the task
     */
    public void missTask(String taskId, LocalDate date, String userId) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[TaskService] Marking task as missed: taskId={}, date={}, userId={}", 
                    taskId, date, userId);
        }

        // Validate task exists and user owns it
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        validateOwnership(task, userId);

        dailyPlanRepository.findByUserIdAndDay(userId, date)
                .ifPresent(plan -> {
                    plan.markMissed(taskId);
                    dailyPlanRepository.save(plan);

                    if (LogUtil.isDebugEnabled()) {
                        LOG.debug("[TaskService] Task marked as missed: taskId={}, date={}", 
                                taskId, date);
                    }
                });
    }

    /**
     * Validates that the specified user owns the given task.
     * <p>
     * This is a security check to ensure users can only modify tasks they own.
     *
     * @param task the task to validate (must not be null)
     * @param userId the user identifier to validate against (must not be null)
     * @throws AuthorizationException if the user does not own the task
     */
    private void validateOwnership(Task task, String userId) {
        if (!task.getUserId().equals(userId)) {
            throw new AuthorizationException(
                    String.format("User %s is not authorized to access task %s", userId, task.getId()));
        }
    }
}
