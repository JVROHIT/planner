package com.personal.planner.domain.nudge;

import com.personal.planner.events.DayClosed;
import com.personal.planner.events.DomainEvent;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Rule: If streak breaks, send reflective nudge.
 */
@Component
public class StreakAtRiskRule implements NudgeRule {

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof DayClosed;
    }

    @Override
    public Optional<Nudge> evaluate(DomainEvent event, Context ctx) {
        // Logic to check if streak reset to zero
        return Optional.empty();
    }
}
