package com.personal.planner.domain.goal;

import com.personal.planner.domain.common.exception.AuthorizationException;
import com.personal.planner.domain.common.exception.GoalNotFoundException;
import com.personal.planner.domain.common.util.LogUtil;
import com.personal.planner.events.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for evaluating progress toward KeyResults using the Strategy pattern.
 * <p>
 * This service implements the Strategy pattern to evaluate different types of key results
 * (Accumulative, Habit, Milestone) based on domain events. Each key result type has its
 * own evaluator strategy that implements the evaluation logic specific to that type.
 * </p>
 * <p>
 * <b>Strategy Pattern Implementation:</b>
 * <ul>
 *   <li><b>Context:</b> This class (KeyResultEvaluator) acts as the context</li>
 *   <li><b>Strategy Interface:</b> The evaluator components (AccumulativeKREvaluator,
 *       HabitKREvaluator, MilestoneKREvaluator) act as concrete strategies</li>
 *   <li><b>Strategy Selection:</b> The {@code supports()} method determines which strategy
 *       to use based on the key result type</li>
 *   <li><b>Strategy Execution:</b> The {@code handle()} method executes the selected strategy</li>
 * </ul>
 * </p>
 * <p>
 * <b>Domain Principles:</b>
 * <ul>
 *   <li>"Goals do not poll history. They react to facts."</li>
 *   <li>"Evaluation is event-driven, not time-driven."</li>
 * </ul>
 * </p>
 * <p>
 * <b>Constraints:</b>
 * <ul>
 *   <li>MUST NEVER modify historical snapshots</li>
 *   <li>MUST ONLY react to domain events to update current evaluative progress</li>
 *   <li>MUST validate ownership before processing events</li>
 * </ul>
 * </p>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
@Service
public class KeyResultEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(KeyResultEvaluator.class);

    private final AccumulativeKREvaluator accumulativeKREvaluator;
    private final HabitKREvaluator habitKREvaluator;
    private final MilestoneKREvaluator milestoneKREvaluator;
    private final KeyResultRepository keyResultRepository;
    private final GoalRepository goalRepository;

    /**
     * Constructs a new KeyResultEvaluator with the required strategy implementations.
     *
     * @param accumulativeKREvaluator the strategy for evaluating accumulative key results
     * @param habitKREvaluator the strategy for evaluating habit key results
     * @param milestoneKREvaluator the strategy for evaluating milestone key results
     * @param keyResultRepository the repository for persisting key results
     * @param goalRepository the repository for validating goal ownership
     */
    public KeyResultEvaluator(AccumulativeKREvaluator accumulativeKREvaluator,
            HabitKREvaluator habitKREvaluator,
            MilestoneKREvaluator milestoneKREvaluator,
            KeyResultRepository keyResultRepository,
            GoalRepository goalRepository) {
        this.accumulativeKREvaluator = accumulativeKREvaluator;
        this.habitKREvaluator = habitKREvaluator;
        this.milestoneKREvaluator = milestoneKREvaluator;
        this.keyResultRepository = keyResultRepository;
        this.goalRepository = goalRepository;
    }

    /**
     * Evaluates a key result based on a domain event using the appropriate strategy.
     * <p>
     * This method implements the Strategy pattern by:
     * <ol>
     *   <li>Validating that the key result's goal belongs to the event's user</li>
     *   <li>Selecting the appropriate evaluator strategy based on key result type</li>
     *   <li>Executing the selected strategy to update progress</li>
     *   <li>Persisting the updated key result</li>
     * </ol>
     * </p>
     *
     * @param keyResult the key result to evaluate
     * @param event the domain event that triggered the evaluation
     * @throws GoalNotFoundException if the associated goal does not exist
     * @throws AuthorizationException if the goal does not belong to the event's user
     */
    public void onEvent(KeyResult keyResult, DomainEvent event) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[KeyResultEvaluator] Evaluating key result: {} for event: {}", 
                    keyResult.getId(), event.eventId());
        }

        // Validate ownership
        Goal goal = goalRepository.findById(keyResult.getGoalId())
                .orElseThrow(() -> new GoalNotFoundException(keyResult.getGoalId()));
        
        if (!goal.getUserId().equals(event.userId())) {
            throw new AuthorizationException(
                    "User " + event.userId() + " is not authorized to evaluate key result " + 
                    keyResult.getId() + " for goal " + goal.getId());
        }

        // Select and execute appropriate strategy
        if (accumulativeKREvaluator.supports(keyResult)) {
            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[KeyResultEvaluator] Using AccumulativeKREvaluator for key result: {}", 
                        keyResult.getId());
            }
            accumulativeKREvaluator.handle(keyResult, event);
        } else if (habitKREvaluator.supports(keyResult)) {
            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[KeyResultEvaluator] Using HabitKREvaluator for key result: {}", 
                        keyResult.getId());
            }
            habitKREvaluator.handle(keyResult, event);
        } else if (milestoneKREvaluator.supports(keyResult)) {
            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[KeyResultEvaluator] Using MilestoneKREvaluator for key result: {}", 
                        keyResult.getId());
            }
            milestoneKREvaluator.handle(keyResult, event);
        }

        keyResultRepository.save(keyResult);
        
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[KeyResultEvaluator] Key result evaluation completed: {}", keyResult.getId());
        }
    }
}
