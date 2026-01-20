package com.personal.planner.domain.goal;

import com.personal.planner.events.DomainEvent;
import org.springframework.stereotype.Component;

/**
 * Evaluator for Milestone-based Key Results.
 */
@Component
public class MilestoneKREvaluator {

    public boolean supports(KeyResult kr) {
        return kr.getType() == KeyResult.Type.MILESTONE;
    }

    public void handle(KeyResult kr, DomainEvent event) {
        // Milestone evaluations are often manual or triggered by specific domain
        // events.
        // For the MVP loop, they can be manually completed via GoalService or react to
        // a specific fact.
    }
}
