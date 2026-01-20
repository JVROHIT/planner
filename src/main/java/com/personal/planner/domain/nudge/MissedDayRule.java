package com.personal.planner.domain.nudge;

import com.personal.planner.events.DayClosed;
import com.personal.planner.events.DomainEvent;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Rule: If a day closes with 0 completion, suggest a lighter plan tomorrow.
 */
@Component
public class MissedDayRule implements NudgeRule {

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof DayClosed;
    }

    @Override
    public Optional<Nudge> evaluate(DomainEvent event, Context ctx) {
        // Logic to check if completion was 0 in DayClosed fact
        return Optional.empty();
    }
}
