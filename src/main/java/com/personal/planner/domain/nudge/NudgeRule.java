package com.personal.planner.domain.nudge;

import com.personal.planner.events.DomainEvent;
import java.util.Optional;

/**
 * Pure rule. Reads facts. Emits suggestions.
 * <p>
 * "Translates meaning into potential guidance."
 * </p>
 * <p>
 * Constraints:
 * - Read-only access to state.
 * - No domain mutation.
 * </p>
 */
public interface NudgeRule {

    /**
     * Identifies if this rule reacts to the given factual event.
     */
    boolean supports(DomainEvent event);

    /**
     * Evaluates the event and state to potentially emit a nudge.
     */
    Optional<Nudge> evaluate(DomainEvent event, Context ctx);

    /**
     * Contextual information available to rules (stubs/placeholders).
     */
    interface Context {
        // Access to meaning repositories like StreakRepository if needed by evaluators
    }
}
