package com.personal.planner.domain.goal;

import com.personal.planner.events.DomainEvent;
import com.personal.planner.events.TaskCompleted;

import org.springframework.stereotype.Component;

/**
 * Evaluator for Milestone-based Key Results.
 */
@Component
public class MilestoneKREvaluator {

    public MilestoneKREvaluator() {
    }

    public boolean supports(KeyResult kr) {
        return kr.getType() == KeyResult.Type.MILESTONE;
    }

    public void handle(KeyResult kr, DomainEvent event) {
        if (event instanceof TaskCompleted) {
            TaskCompleted taskCompleted = (TaskCompleted) event;
            if (kr.getId().equals(taskCompleted.getKeyResultId())) {
                kr.applyProgress(1);
            }
        }
    }
}
