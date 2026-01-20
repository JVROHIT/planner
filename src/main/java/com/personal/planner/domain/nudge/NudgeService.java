package com.personal.planner.domain.nudge;

import com.personal.planner.events.DomainEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Translates meaning into gentle prompts.
 * <p>
 * Constraints:
 * - Must not modify plans, tasks, goals, or streaks.
 * </p>
 */
@Service
public class NudgeService {

    private final List<NudgeRule> rules;
    private final NudgeRepository nudgeRepository;

    public NudgeService(List<NudgeRule> rules, NudgeRepository nudgeRepository) {
        this.rules = rules;
        this.nudgeRepository = nudgeRepository;
    }

    @EventListener
    public void on(DomainEvent event) {
        NudgeRule.Context ctx = new NudgeRule.Context() {
            // Placeholder implementation
        };

        rules.stream()
                .filter(rule -> rule.supports(event))
                .map(rule -> rule.evaluate(event, ctx))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .forEach(nudgeRepository::save);
    }
}
