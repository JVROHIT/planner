package com.personal.planner.domain.goal;

import com.personal.planner.events.DomainEvent;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service for evaluating progress toward KeyResults.
 * <p>
 * "Goals do not poll history. They react to facts."
 * "Evaluation is event-driven, not time-driven."
 * </p>
 * <p>
 * Constraints:
 * - MUST NEVER modify historical snapshots.
 * - MUST ONLY react to domain events to update current evaluative progress.
 * </p>
 */
@Service
public class KeyResultEvaluator {

    private final AccumulativeKREvaluator accumulativeKREvaluator;
    private final HabitKREvaluator habitKREvaluator;
    private final MilestoneKREvaluator milestoneKREvaluator;
    private final KeyResultRepository keyResultRepository;

    public KeyResultEvaluator(AccumulativeKREvaluator accumulativeKREvaluator,
            HabitKREvaluator habitKREvaluator,
            MilestoneKREvaluator milestoneKREvaluator,
            KeyResultRepository keyResultRepository) {
        this.accumulativeKREvaluator = accumulativeKREvaluator;
        this.habitKREvaluator = habitKREvaluator;
        this.milestoneKREvaluator = milestoneKREvaluator;
        this.keyResultRepository = keyResultRepository;
    }

    /**
     * Strategy contract for evaluating a specific KeyResult against a fact.
     */
    public void onEvent(KeyResult keyResult, DomainEvent event) {
        if (accumulativeKREvaluator.supports(keyResult)) {
            accumulativeKREvaluator.handle(keyResult, event);
        } else if (habitKREvaluator.supports(keyResult)) {
            habitKREvaluator.handle(keyResult, event);
        } else if (milestoneKREvaluator.supports(keyResult)) {
            milestoneKREvaluator.handle(keyResult, event);
        }

        keyResultRepository.save(keyResult);
    }
}
