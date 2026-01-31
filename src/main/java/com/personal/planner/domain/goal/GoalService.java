package com.personal.planner.domain.goal;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.EventReceipt;
import com.personal.planner.domain.common.EventReceiptRepository;
import com.personal.planner.domain.common.constants.EventConstants;
import com.personal.planner.domain.common.exception.AuthorizationException;
import com.personal.planner.domain.common.exception.GoalNotFoundException;
import com.personal.planner.domain.common.exception.KeyResultNotFoundException;
import com.personal.planner.domain.common.util.LogUtil;
import com.personal.planner.domain.user.UserTimeZoneService;
import com.personal.planner.events.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.personal.planner.domain.common.constants.TimeConstants.ZONE_OFFSET;

/**
 * Service for managing directional goals and their key results.
 * <p>
 * This service provides operations for creating, updating, and deleting goals and key results.
 * It also handles event-driven evaluation of key results through the {@link KeyResultEvaluator}.
 * </p>
 * <p>
 * All operations enforce ownership validation to ensure users can only access and modify
 * their own goals and key results.
 * </p>
 * <p>
 * Key responsibilities:
 * <ul>
 *   <li>CRUD operations for goals and key results</li>
 *   <li>Event-driven key result evaluation</li>
 *   <li>Manual milestone completion</li>
 *   <li>Ownership validation for all operations</li>
 * </ul>
 * </p>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
@Service
public class GoalService {

    private static final Logger LOG = LoggerFactory.getLogger(GoalService.class);

    private final GoalRepository goalRepository;
    private final KeyResultRepository keyResultRepository;
    private final KeyResultEvaluator keyResultEvaluator;
    private final EventReceiptRepository eventReceiptRepository;
    private final UserTimeZoneService timeZoneService;
    private final ClockProvider clock;

    /**
     * Constructs a new GoalService with the required dependencies.
     *
     * @param goalRepository the repository for goal persistence
     * @param keyResultRepository the repository for key result persistence
     * @param keyResultEvaluator the evaluator for updating key result progress
     * @param eventReceiptRepository the repository for tracking processed events
     * @param clock the clock provider for timestamp generation
     */
    public GoalService(GoalRepository goalRepository,
            KeyResultRepository keyResultRepository,
            KeyResultEvaluator keyResultEvaluator,
            EventReceiptRepository eventReceiptRepository,
            UserTimeZoneService timeZoneService,
            ClockProvider clock) {
        this.goalRepository = goalRepository;
        this.keyResultRepository = keyResultRepository;
        this.keyResultEvaluator = keyResultEvaluator;
        this.eventReceiptRepository = eventReceiptRepository;
        this.timeZoneService = timeZoneService;
        this.clock = clock;
    }

    /**
     * Event listener that processes domain events to update key result progress.
     * <p>
     * This method ensures idempotent processing by checking event receipts before
     * processing. It finds all goals for the event's user and evaluates their
     * associated key results based on the event type.
     * </p>
     *
     * @param event the domain event to process
     */
    @EventListener
    public void on(DomainEvent event) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Processing event: {} for user: {}", event.eventId(), event.userId());
        }

        if (eventReceiptRepository.findByEventIdAndConsumer(event.eventId(), EventConstants.CONSUMER_GOAL).isPresent()) {
            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[GoalService] Event {} already processed, skipping", event.eventId());
            }
            return;
        }

        goalRepository.findByUserId(event.userId()).forEach(goal -> {
            keyResultRepository.findByGoalId(goal.getId()).forEach(kr -> {
                keyResultEvaluator.onEvent(kr, event);
            });
        });

        eventReceiptRepository.save(EventReceipt.of(event.eventId(), EventConstants.CONSUMER_GOAL, clock.now().toInstant(ZONE_OFFSET)));
        
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Event {} processed successfully", event.eventId());
        }
    }

    /**
     * Creates a new goal for the specified user.
     * <p>
     * The goal's userId must match the provided userId to ensure ownership.
     * </p>
     *
     * @param goal the goal to create
     * @param userId the user ID of the goal owner
     * @return the created goal
     * @throws AuthorizationException if the goal's userId does not match the provided userId
     */
    public Goal createGoal(Goal goal, String userId) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Creating goal for user: {}", userId);
        }

        validateGoalOwnership(goal, userId);
        applyGoalDefaults(goal, userId);
        Goal created = goalRepository.save(goal);
        
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Goal created with ID: {}", created.getId());
        }
        
        return created;
    }

    /**
     * Updates an existing goal.
     * <p>
     * The goal must exist and belong to the specified user.
     * </p>
     *
     * @param goal the goal to update
     * @param userId the user ID of the goal owner
     * @return the updated goal
     * @throws GoalNotFoundException if the goal does not exist
     * @throws AuthorizationException if the goal does not belong to the user
     */
    public Goal updateGoal(Goal goal, String userId) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Updating goal: {} for user: {}", goal.getId(), userId);
        }

        Goal existing = goalRepository.findById(goal.getId())
                .orElseThrow(() -> new GoalNotFoundException(goal.getId()));
        
        validateGoalOwnership(existing, userId);
        
        // Merge updates
        existing.setTitle(goal.getTitle() != null ? goal.getTitle() : existing.getTitle());
        existing.setHorizon(goal.getHorizon() != null ? goal.getHorizon() : existing.getHorizon());
        existing.setStartDate(goal.getStartDate() != null ? goal.getStartDate() : existing.getStartDate());
        existing.setEndDate(goal.getEndDate() != null ? goal.getEndDate() : existing.getEndDate());
        existing.setStatus(goal.getStatus() != null ? goal.getStatus() : existing.getStatus());

        applyGoalDefaults(existing, userId);
        Goal updated = goalRepository.save(existing);
        
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Goal updated: {}", updated.getId());
        }
        
        return updated;
    }

    /**
     * Deletes a goal by its identifier.
     * <p>
     * The goal must exist and belong to the specified user.
     * </p>
     *
     * @param goalId the identifier of the goal to delete
     * @param userId the user ID of the goal owner
     * @throws GoalNotFoundException if the goal does not exist
     * @throws AuthorizationException if the goal does not belong to the user
     */
    public void deleteGoal(String goalId, String userId) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Deleting goal: {} for user: {}", goalId, userId);
        }

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalNotFoundException(goalId));
        
        validateGoalOwnership(goal, userId);
        goalRepository.deleteById(goalId);
        
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Goal deleted: {}", goalId);
        }
    }

    /**
     * Creates a new key result for a goal.
     * <p>
     * The associated goal must exist and belong to the specified user.
     * </p>
     *
     * @param kr the key result to create
     * @param userId the user ID of the goal owner
     * @return the created key result
     * @throws GoalNotFoundException if the associated goal does not exist
     * @throws AuthorizationException if the goal does not belong to the user
     */
    public KeyResult createKeyResult(KeyResult kr, String userId) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Creating key result for goal: {} and user: {}", kr.getGoalId(), userId);
        }

        Goal goal = goalRepository.findById(kr.getGoalId())
                .orElseThrow(() -> new GoalNotFoundException(kr.getGoalId()));
        
        validateGoalOwnership(goal, userId);

        if (kr.getWeight() <= 0) {
            kr.setWeight(1.0);
        }
        if (kr.getCurrentValue() == 0 && kr.getStartValue() != 0) {
            kr.setCurrentValue(kr.getStartValue());
        }
        kr.updateProgress(kr.getCurrentValue());

        KeyResult created = keyResultRepository.save(kr);
        
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Key result created with ID: {}", created.getId());
        }
        
        return created;
    }

    /**
     * Updates an existing key result.
     * <p>
     * The key result must exist and its associated goal must belong to the specified user.
     * </p>
     *
     * @param kr the key result to update
     * @param userId the user ID of the goal owner
     * @return the updated key result
     * @throws KeyResultNotFoundException if the key result does not exist
     * @throws GoalNotFoundException if the associated goal does not exist
     * @throws AuthorizationException if the goal does not belong to the user
     */
    public KeyResult updateKeyResult(KeyResult kr, String userId) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Updating key result: {} for user: {}", kr.getId(), userId);
        }

        KeyResult existing = keyResultRepository.findById(kr.getId())
                .orElseThrow(() -> new KeyResultNotFoundException(kr.getId()));
        
        Goal goal = goalRepository.findById(existing.getGoalId())
                .orElseThrow(() -> new GoalNotFoundException(existing.getGoalId()));
        
        validateGoalOwnership(goal, userId);
        
        existing.setTitle(kr.getTitle() != null ? kr.getTitle() : existing.getTitle());
        existing.setType(kr.getType() != null ? kr.getType() : existing.getType());
        if (kr.getTargetValue() > 0) {
            existing.setTargetValue(kr.getTargetValue());
        }
        if (kr.getStartValue() != 0) {
            existing.setStartValue(kr.getStartValue());
        }
        if (kr.getCurrentValue() != 0 || kr.getStartValue() != 0) {
            existing.setCurrentValue(kr.getCurrentValue());
        }
        if (kr.getWeight() > 0) {
            existing.setWeight(kr.getWeight());
        }
        existing.updateProgress(existing.getCurrentValue());

        KeyResult updated = keyResultRepository.save(existing);
        
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Key result updated: {}", updated.getId());
        }
        
        return updated;
    }

    /**
     * Deletes a key result by its identifier.
     * <p>
     * The key result must exist and its associated goal must belong to the specified user.
     * </p>
     *
     * @param keyResultId the identifier of the key result to delete
     * @param userId the user ID of the goal owner
     * @throws KeyResultNotFoundException if the key result does not exist
     * @throws GoalNotFoundException if the associated goal does not exist
     * @throws AuthorizationException if the goal does not belong to the user
     */
    public void deleteKeyResult(String keyResultId, String userId) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Deleting key result: {} for user: {}", keyResultId, userId);
        }

        KeyResult kr = keyResultRepository.findById(keyResultId)
                .orElseThrow(() -> new KeyResultNotFoundException(keyResultId));
        
        Goal goal = goalRepository.findById(kr.getGoalId())
                .orElseThrow(() -> new GoalNotFoundException(kr.getGoalId()));
        
        validateGoalOwnership(goal, userId);
        keyResultRepository.deleteById(keyResultId);
        
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Key result deleted: {}", keyResultId);
        }
    }

    /**
     * Manually completes a milestone key result.
     * <p>
     * This method allows manual completion of milestone key results, setting their
     * progress to the target value. The key result must exist, be of type MILESTONE,
     * and its associated goal must belong to the specified user.
     * </p>
     *
     * @param keyResultId the identifier of the milestone key result to complete
     * @param userId the user ID of the goal owner
     * @throws KeyResultNotFoundException if the key result does not exist
     * @throws GoalNotFoundException if the associated goal does not exist
     * @throws AuthorizationException if the goal does not belong to the user
     * @throws IllegalArgumentException if the key result is not of type MILESTONE
     */
    public void completeMilestone(String keyResultId, String userId) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Completing milestone: {} for user: {}", keyResultId, userId);
        }

        KeyResult kr = keyResultRepository.findById(keyResultId)
                .orElseThrow(() -> new KeyResultNotFoundException(keyResultId));
        
        Goal goal = goalRepository.findById(kr.getGoalId())
                .orElseThrow(() -> new GoalNotFoundException(kr.getGoalId()));
        
        validateGoalOwnership(goal, userId);
        
        if (kr.getType() != KeyResult.Type.MILESTONE) {
            throw new IllegalArgumentException("Key result " + keyResultId + " is not a milestone");
        }
        
        kr.updateProgress(kr.getTargetValue());
        keyResultRepository.save(kr);
        
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[GoalService] Milestone completed: {}", keyResultId);
        }
    }

    /**
     * Validates that a goal belongs to the specified user.
     *
     * @param goal the goal to validate
     * @param userId the user ID to check against
     * @throws AuthorizationException if the goal does not belong to the user
     */
    private void validateGoalOwnership(Goal goal, String userId) {
        if (!goal.getUserId().equals(userId)) {
            throw new AuthorizationException(
                    "User " + userId + " is not authorized to access goal " + goal.getId());
        }
    }

    private void applyGoalDefaults(Goal goal, String userId) {
        if (goal.getHorizon() == null) {
            goal.setHorizon(Goal.Horizon.MONTH);
        }
        if (goal.getStatus() == null) {
            goal.setStatus(Goal.Status.ACTIVE);
        }
        if (goal.getStartDate() == null) {
            goal.setStartDate(clock.today(timeZoneService.resolveZone(userId)));
        }
        if (goal.getEndDate() == null) {
            switch (goal.getHorizon()) {
                case QUARTER -> goal.setEndDate(goal.getStartDate().plusMonths(3));
                case YEAR -> goal.setEndDate(goal.getStartDate().plusYears(1));
                default -> goal.setEndDate(goal.getStartDate().plusMonths(1));
            }
        }
    }
}
