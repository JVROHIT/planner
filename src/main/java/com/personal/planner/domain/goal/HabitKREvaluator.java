package com.personal.planner.domain.goal;

import com.personal.planner.events.DayClosed;
import com.personal.planner.events.DomainEvent;
import org.springframework.stereotype.Component;

/**
 * Evaluator for Habit-based Key Results.
 * <p>
 * Invariant:
 * - Habit KRs react primarily to {@link DayClosed} events to verify daily
 * consistency.
 * </p>
 */
@Component
public class HabitKREvaluator {

    /**
     * @return true if the KeyResult type is habit-based (e.g., "Mediate every
     *         day").
     */
    public boolean supports(KeyResult kr) {
        return kr.getType() == KeyResult.Type.HABIT;
    }

    /**
     * Handles the event, checking if the habit was maintained at the point of day
     * closure.
     */
    public void handle(KeyResult kr, DomainEvent event) {
        if (event instanceof DayClosed) {
            // // Logic to verify habit consistency for the closed day
            // // Mutation: update kr.currentProgress
            kr.setCurrentProgress(kr.getCurrentProgress() + 1); // Mock increment
        }
    }
}
