package com.personal.planner.domain.goal;

import com.personal.planner.domain.common.util.LogUtil;
import com.personal.planner.events.DomainEvent;
import com.personal.planner.events.TaskCompleted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Strategy implementation for evaluating Milestone-based Key Results.
 * <p>
 * Milestone key results track progress by counting completed tasks linked to the milestone.
 * Each completed task increments the progress by 1, representing progress toward the milestone.
 * </p>
 * <p>
 * Evaluation logic:
 * <ul>
 *   <li>Listens for {@link TaskCompleted} events</li>
 *   <li>Checks if the completed task is linked to this milestone key result</li>
 *   <li>If linked, increments progress by 1</li>
 * </ul>
 * </p>
 * <p>
 * Note: Milestones can also be manually completed via {@link GoalService#completeMilestone(String, String)},
 * which sets the progress to the target value directly.
 * </p>
 * <p>
 * This evaluator is part of the Strategy pattern implementation for key result evaluation.
 * </p>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
@Component
public class MilestoneKREvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(MilestoneKREvaluator.class);

    /**
     * Constructs a new MilestoneKREvaluator.
     */
    public MilestoneKREvaluator() {
    }

    /**
     * Determines if this evaluator supports the given key result type.
     *
     * @param kr the key result to check
     * @return true if the key result type is MILESTONE, false otherwise
     */
    public boolean supports(KeyResult kr) {
        return kr.getType() == KeyResult.Type.MILESTONE;
    }

    /**
     * Handles a domain event to update a milestone key result's progress.
     * <p>
     * This method processes {@link TaskCompleted} events and increments the milestone
     * progress by 1 if the completed task is linked to this milestone key result.
     * </p>
     *
     * @param kr the milestone key result to update
     * @param event the domain event that triggered the evaluation
     */
    public void handle(KeyResult kr, DomainEvent event) {
        if (event instanceof TaskCompleted) {
            TaskCompleted taskCompleted = (TaskCompleted) event;
            
            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[MilestoneKREvaluator] Processing TaskCompleted event for task: {} and key result: {}", 
                        taskCompleted.getTaskId(), kr.getId());
            }
            
            // Only process if the task is linked to this milestone key result
            if (kr.getId().equals(taskCompleted.getKeyResultId())) {
                if (LogUtil.isDebugEnabled()) {
                    LOG.debug("[MilestoneKREvaluator] Task linked to milestone, incrementing progress for key result: {}", 
                            kr.getId());
                }
                kr.applyProgress(1);
            } else {
                if (LogUtil.isDebugEnabled()) {
                    LOG.debug("[MilestoneKREvaluator] Task {} not linked to milestone key result {}, skipping", 
                            taskCompleted.getTaskId(), kr.getId());
                }
            }
        }
    }
}
