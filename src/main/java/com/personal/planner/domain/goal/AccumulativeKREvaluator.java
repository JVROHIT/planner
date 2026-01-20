package com.personal.planner.domain.goal;

import com.personal.planner.events.DomainEvent;
import com.personal.planner.events.TaskCompleted;
import org.springframework.stereotype.Component;

/**
 * Evaluator for Accumulative Key Results.
 */
@Component
public class AccumulativeKREvaluator {

    public boolean supports(KeyResult kr) {
        return kr.getType() == KeyResult.Type.ACCUMULATIVE;
    }

    public void handle(KeyResult kr, DomainEvent event) {
        if (event instanceof TaskCompleted) {
            kr.applyProgress(1.0);
        }
    }
}
