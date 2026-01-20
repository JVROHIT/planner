package com.personal.planner.domain.nudge;

import com.personal.planner.events.DomainEvent;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Rule: If no task completed by noon, remind.
 */
@Component
public class IdleMorningRule implements NudgeRule {

    @Override
    public boolean supports(DomainEvent event) {
        return false;
    }

    @Override
    public Optional<Nudge> evaluate(DomainEvent event, Context ctx) {
        return Optional.empty();
    }
}
