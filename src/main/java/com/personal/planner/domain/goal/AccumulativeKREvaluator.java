package com.personal.planner.domain.goal;

import com.personal.planner.events.DomainEvent;
import com.personal.planner.events.TaskCompleted;
import org.springframework.stereotype.Component;

/**
 * Evaluator for Accumulative Key Results.
 * <p>
 * Invariant:
 * - Accumulative KRs react primarily to {@link TaskCompleted} events.
 * </p>
 */
@Component
public class AccumulativeKREvaluator {

    /**
     * @return true if the KeyResult type is accumulative (e.g., "Complete 50
     *         tasks").
     */
    public boolean supports(KeyResult kr) {
        return kr.getType() == KeyResult.Type.ACCUMULATIVE;
    }

    /**
     * Handles the event, typically incrementing progress when a TaskCompleted event
     * is received.
     */
    public void handle(KeyResult kr, DomainEvent event) {
        if (event instanceof TaskCompleted) {
            kr.setCurrentProgress(kr.getCurrentProgress() + 1);
        }
    }
}
