package com.personal.planner.domain.goal;

import com.personal.planner.events.DomainEvent;
import org.springframework.stereotype.Component;

/**
 * Evaluator for Milestone-based Key Results.
 * <p>
 * Invariant:
 * - Milestone KRs react to explicit completion events or state transitions.
 * </p>
 */
@Component
public class MilestoneKREvaluator {

    /**
     * @return true if the KeyResult type is milestone-based (e.g., "Launch version
     *         1.0").
     */
    public boolean supports(KeyResult kr) {
        return kr.getType() == KeyResult.Type.MILESTONE;
    }

    /**
     * Handles the event, marking the milestone as reached when the specific fact is
     * received.
     */
    public void handle(KeyResult kr, DomainEvent event) {
        // // Logic to identify if the event fulfills the milestone
        // Mutation: set kr.currentProgress to kr.targetValue
        kr.setCurrentProgress(kr.getTargetValue());
    }
}
