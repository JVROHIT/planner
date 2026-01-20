package com.personal.planner.domain.goal;

import com.personal.planner.events.DayClosed;
import com.personal.planner.events.DomainEvent;
import org.springframework.stereotype.Component;

/**
 * Evaluator for Habit-based Key Results.
 */
@Component
public class HabitKREvaluator {

    public boolean supports(KeyResult kr) {
        return kr.getType() == KeyResult.Type.HABIT;
    }

    public void handle(KeyResult kr, DomainEvent event) {
        if (event instanceof DayClosed) {
            // Mock increment for now as per "ugly internals are fine"
            kr.applyProgress(1.0);
        }
    }
}
