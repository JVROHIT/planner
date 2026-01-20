package com.personal.planner.domain.goal;

import com.personal.planner.events.DomainEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service for managing directional goals.
 * <p>
 * "All cross-domain effects flow through events."
 * "This preserves temporal truth and decoupling."
 * </p>
 * <p>
 * Constraints:
 * - MUST NEVER perform calculations of historical progress (Use GoalSnapshot).
 * - MUST ONLY manage the current definition and intention of objectives.
 * - Goals never recompute from history (Event-driven only).
 * </p>
 */
@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final KeyResultRepository keyResultRepository;
    private final KeyResultEvaluator keyResultEvaluator;

    public GoalService(GoalRepository goalRepository,
            KeyResultRepository keyResultRepository,
            KeyResultEvaluator keyResultEvaluator) {
        this.goalRepository = goalRepository;
        this.keyResultRepository = keyResultRepository;
        this.keyResultEvaluator = keyResultEvaluator;
    }

    /**
     * Entry point for all goal-related reactions.
     * <p>
     * "Routes events to the correct KeyResult evaluator."
     * </p>
     * <p>
     * Constraints:
     * - Must never read DailyPlans or compute streaks.
     * </p>
     */
    @EventListener
    public void on(DomainEvent event) {
        goalRepository.findByUserId(event.userId()).forEach(goal -> {
            keyResultRepository.findByGoalId(goal.getId()).forEach(kr -> {
                keyResultEvaluator.onEvent(kr, event);
            });
        });
    }

    public void createGoal(Goal goal) {
        goalRepository.save(goal);
    }

    public void updateGoal(Goal goal) {
        goalRepository.save(goal);
    }
}
