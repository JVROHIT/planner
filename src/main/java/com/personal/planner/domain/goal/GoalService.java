package com.personal.planner.domain.goal;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.EventReceipt;
import com.personal.planner.domain.common.EventReceiptRepository;
import com.personal.planner.events.DomainEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service for managing directional goals.
 */
@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final KeyResultRepository keyResultRepository;
    private final KeyResultEvaluator keyResultEvaluator;
    private final EventReceiptRepository eventReceiptRepository;
    private final ClockProvider clock;

    private static final String CONSUMER_NAME = "GOAL";

    public GoalService(GoalRepository goalRepository,
            KeyResultRepository keyResultRepository,
            KeyResultEvaluator keyResultEvaluator,
            EventReceiptRepository eventReceiptRepository,
            ClockProvider clock) {
        this.goalRepository = goalRepository;
        this.keyResultRepository = keyResultRepository;
        this.keyResultEvaluator = keyResultEvaluator;
        this.eventReceiptRepository = eventReceiptRepository;
        this.clock = clock;
    }

    @EventListener
    public void on(DomainEvent event) {
        if (eventReceiptRepository.findByEventIdAndConsumer(event.eventId(), CONSUMER_NAME).isPresent()) {
            return;
        }

        goalRepository.findByUserId(event.userId()).forEach(goal -> {
            keyResultRepository.findByGoalId(goal.getId()).forEach(kr -> {
                keyResultEvaluator.onEvent(kr, event);
            });
        });

        eventReceiptRepository.save(EventReceipt.of(event.eventId(), CONSUMER_NAME, clock.now()));
    }

    public Goal createGoal(Goal goal) {
        return goalRepository.save(goal);
    }

    public Goal updateGoal(Goal goal) {
        return goalRepository.save(goal);
    }

    public void deleteGoal(String id) {
        goalRepository.deleteById(id);
    }

    public KeyResult createKeyResult(KeyResult kr) {
        return keyResultRepository.save(kr);
    }

    public KeyResult updateKeyResult(KeyResult kr) {
        return keyResultRepository.save(kr);
    }

    public void deleteKeyResult(String id) {
        keyResultRepository.deleteById(id);
    }

    public void completeMilestone(String keyResultId) {
        keyResultRepository.findById(keyResultId).ifPresent(kr -> {
            if (kr.getType() == KeyResult.Type.MILESTONE) {
                // Evaluation logic is usually event driven, but manual completion is allowed
                // for Milestones in spec.
                // Reusing evaluator if possible or direct.
                kr.updateProgress(kr.getTargetValue());
                keyResultRepository.save(kr);
            }
        });
    }
}
