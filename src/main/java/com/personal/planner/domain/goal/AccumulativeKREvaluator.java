package com.personal.planner.domain.goal;

import com.personal.planner.domain.common.util.LogUtil;
import com.personal.planner.domain.task.Task;
import com.personal.planner.domain.task.TaskRepository;
import com.personal.planner.events.DomainEvent;
import com.personal.planner.events.TaskCompleted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Strategy implementation for evaluating Accumulative Key Results.
 * <p>
 * Accumulative key results track progress by accumulating contributions from completed tasks.
 * When a task linked to this key result is completed, the task's contribution value is
 * added to the key result's current value.
 * </p>
 * <p>
 * Evaluation logic:
 * <ul>
 *   <li>Listens for {@link TaskCompleted} events</li>
 *   <li>Checks if the completed task is linked to this key result</li>
 *   <li>Applies the task's contribution value to the key result's progress</li>
 * </ul>
 * </p>
 * <p>
 * This evaluator is part of the Strategy pattern implementation for key result evaluation.
 * </p>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
@Component
public class AccumulativeKREvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(AccumulativeKREvaluator.class);

    private final TaskRepository taskRepository;

    /**
     * Constructs a new AccumulativeKREvaluator with the required dependencies.
     *
     * @param taskRepository the repository for retrieving task information
     */
    public AccumulativeKREvaluator(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Determines if this evaluator supports the given key result type.
     *
     * @param kr the key result to check
     * @return true if the key result type is ACCUMULATIVE, false otherwise
     */
    public boolean supports(KeyResult kr) {
        return kr.getType() == KeyResult.Type.ACCUMULATIVE;
    }

    /**
     * Handles a domain event to update an accumulative key result's progress.
     * <p>
     * This method processes {@link TaskCompleted} events and applies the task's
     * contribution to the key result if the task is linked to this key result.
     * </p>
     *
     * @param kr the accumulative key result to update
     * @param event the domain event that triggered the evaluation
     */
    public void handle(KeyResult kr, DomainEvent event) {
        if (event instanceof TaskCompleted) {
            TaskCompleted taskCompleted = (TaskCompleted) event;
            
            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[AccumulativeKREvaluator] Processing TaskCompleted event for task: {} and key result: {}", 
                        taskCompleted.getTaskId(), kr.getId());
            }
            
            // Only process if the task is linked to this key result
            if (kr.getId().equals(taskCompleted.getKeyResultId())) {
                Optional<Task> taskOpt = taskRepository.findById(taskCompleted.getTaskId());
                
                if (taskOpt.isPresent()) {
                    Task task = taskOpt.get();
                    double contribution = task.getContribution();
                    
                    if (LogUtil.isDebugEnabled()) {
                        LOG.debug("[AccumulativeKREvaluator] Applying contribution: {} to key result: {}", 
                                contribution, kr.getId());
                    }
                    
                    kr.applyProgress(contribution);
                } else {
                    if (LogUtil.isDebugEnabled()) {
                        LOG.debug("[AccumulativeKREvaluator] Task {} not found, skipping evaluation", 
                                taskCompleted.getTaskId());
                    }
                }
            } else {
                if (LogUtil.isDebugEnabled()) {
                    LOG.debug("[AccumulativeKREvaluator] Task {} not linked to key result {}, skipping", 
                            taskCompleted.getTaskId(), kr.getId());
                }
            }
        }
    }
}
