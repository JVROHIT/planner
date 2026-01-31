package com.personal.planner.domain.streak;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.EventReceipt;
import com.personal.planner.domain.common.EventReceiptRepository;
import com.personal.planner.domain.common.constants.EventConstants;
import com.personal.planner.domain.common.exception.EventProcessingException;
import com.personal.planner.domain.common.util.LogUtil;
import com.personal.planner.domain.plan.DailyPlan;
import com.personal.planner.domain.plan.DailyPlanRepository;
import com.personal.planner.events.DayClosed;
import com.personal.planner.events.UserCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static com.personal.planner.domain.common.constants.TimeConstants.ZONE_OFFSET;

/**
 * Service for calculating behavioral consistency through derived interpretation.
 * <p>
 * <b>Derived Interpretation Concept:</b>
 * StreakState is a <i>derived interpretation</i> of behavioral consistency, not a
 * direct representation. The streak value is never edited directly by users or
 * application code. Instead, it emerges naturally from the sequence of factual
 * events (DayClosed) that represent what actually happened.
 * </p>
 * <p>
 * This service implements the event-sourcing pattern where:
 * <ul>
 *   <li><b>Source of Truth:</b> DailyPlan records (immutable historical facts)</li>
 *   <li><b>Derived State:</b> StreakState (computed interpretation)</li>
 *   <li><b>Computation Rule:</b> Streak increments when all tasks in a closed day are completed,
 *       resets to zero otherwise</li>
 * </ul>
 * </p>
 * <p>
 * <b>Streak Calculation Rules:</b>
 * <ol>
 *   <li>A streak day is counted when a DailyPlan is closed AND all tasks are completed</li>
 *   <li>If any task is incomplete when the day closes, the streak resets to 0</li>
 *   <li>If a day has no tasks (empty plan), it does not count toward the streak</li>
 *   <li>Streak increments by 1 for each consecutive successful day</li>
 *   <li>Streak resets to 0 on the first incomplete day</li>
 * </ol>
 * </p>
 * <p>
 * <b>Constraints:</b>
 * <ul>
 *   <li>Must not read WeeklyPlan or Tasks directly</li>
 *   <li>MUST ONLY derive state from the sequence of closed days</li>
 *   <li>Safe to receive the same event more than once (Idempotent)</li>
 *   <li>StreakState is never modified directly - only through event handlers</li>
 * </ul>
 * </p>
 */
@Service
public class StreakService {

    private static final Logger LOG = LoggerFactory.getLogger(StreakService.class);

    private final StreakRepository streakRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final EventReceiptRepository eventReceiptRepository;
    private final ClockProvider clock;

    public StreakService(StreakRepository streakRepository,
            DailyPlanRepository dailyPlanRepository,
            EventReceiptRepository eventReceiptRepository,
            ClockProvider clock) {
        this.streakRepository = streakRepository;
        this.dailyPlanRepository = dailyPlanRepository;
        this.eventReceiptRepository = eventReceiptRepository;
        this.clock = clock;
    }

    /**
     * Updates the user's streak based on the factual close of a daily cycle.
     * <p>
     * This method implements the derived interpretation pattern by computing streak state
     * from the immutable DailyPlan fact. The streak value emerges from the sequence of
     * closed days, never being directly edited.
     * </p>
     * <p>
     * <b>Processing Flow:</b>
     * <ol>
     *   <li>Check idempotency - skip if already processed</li>
     *   <li>Retrieve the closed DailyPlan for the event day</li>
     *   <li>Load or create StreakState for the user</li>
     *   <li>Calculate streak: increment if all tasks completed, reset otherwise</li>
     *   <li>Persist updated state and record event receipt</li>
     * </ol>
     * </p>
     * <p>
     * <b>Idempotency:</b>
     * Uses EventReceipt to ensure the same DayClosed event is never processed twice,
     * making this handler safe for event replay scenarios.
     * </p>
     *
     * @param event the DayClosed event representing a finalized daily cycle
     * @throws EventProcessingException if event processing fails due to repository errors
     */
    @EventListener
    public void on(DayClosed event) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[StreakService] Processing DayClosed event: eventId={}, userId={}, day={}",
                    event.eventId(), event.userId(), event.getDay());
        }

        if (eventReceiptRepository.findByEventIdAndConsumer(event.eventId(), EventConstants.CONSUMER_STREAK).isPresent()) {
            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[StreakService] Event already processed, skipping: eventId={}", event.eventId());
            }
            return;
        }

        try {
            dailyPlanRepository.findByUserIdAndDay(event.userId(), event.getDay())
                    .ifPresentOrElse(
                            plan -> processDayClosed(event, plan),
                            () -> {
                                if (LogUtil.isDebugEnabled()) {
                                    LOG.debug("[StreakService] No DailyPlan found for userId={}, day={}, skipping streak update",
                                            event.userId(), event.getDay());
                                }
                            }
                    );
        } catch (Exception e) {
            String errorMsg = String.format("Failed to process DayClosed event: eventId=%s, userId=%s, day=%s",
                    event.eventId(), event.userId(), event.getDay());
            LOG.error("[StreakService] {}", errorMsg, e);
            throw new EventProcessingException(errorMsg, e);
        }
    }

    /**
     * Processes a DayClosed event by updating the user's streak state.
     * <p>
     * This method implements the core streak calculation logic:
     * - If all tasks in the closed plan are completed, increment streak
     * - Otherwise, reset streak to zero
     * - If plan has no tasks, streak remains unchanged
     * </p>
     *
     * @param event the DayClosed event
     * @param plan the closed DailyPlan for the day
     */
    private void processDayClosed(DayClosed event, DailyPlan plan) {
        StreakState state = streakRepository.findByUserId(event.userId())
                .orElseGet(() -> {
                    if (LogUtil.isDebugEnabled()) {
                        LOG.debug("[StreakService] Creating new StreakState for userId={}", event.userId());
                    }
                    return StreakState.builder()
                            .userId(event.userId())
                            .currentStreak(0)
                            .build();
                });

        long totalTasks = plan.getEntries().size();
        long completedTasks = plan.getEntries().stream()
                .filter(entry -> entry.getStatus() == DailyPlan.Status.COMPLETED)
                .count();

        int previousStreak = state.getCurrentStreak();

        if (totalTasks > 0 && completedTasks == totalTasks) {
            state.setCurrentStreak(state.getCurrentStreak() + 1);
            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[StreakService] Streak incremented: userId={}, day={}, previousStreak={}, newStreak={}, tasks={}/{}",
                        event.userId(), event.getDay(), previousStreak, state.getCurrentStreak(), completedTasks, totalTasks);
            }
        } else {
            state.setCurrentStreak(0);
            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[StreakService] Streak reset: userId={}, day={}, previousStreak={}, completedTasks={}/{}",
                        event.userId(), event.getDay(), previousStreak, completedTasks, totalTasks);
            }
        }

        streakRepository.save(state);
        eventReceiptRepository.save(EventReceipt.of(event.eventId(), EventConstants.CONSUMER_STREAK,
                clock.now().toInstant(ZONE_OFFSET)));

        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[StreakService] StreakState updated and receipt recorded: userId={}, eventId={}",
                    event.userId(), event.eventId());
        }
    }

    /**
     * Initializes streak state for a newly created user.
     * <p>
     * When a user is created, this handler establishes the initial StreakState with
     * a streak of zero. This is the starting point for the derived interpretation,
     * which will evolve as DayClosed events are processed.
     * </p>
     * <p>
     * <b>Idempotency:</b>
     * Uses EventReceipt to ensure the same UserCreated event is never processed twice.
     * </p>
     *
     * @param event the UserCreated event representing a new user registration
     * @throws EventProcessingException if event processing fails due to repository errors
     */
    @EventListener
    public void on(UserCreated event) {
        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[StreakService] Processing UserCreated event: eventId={}, userId={}",
                    event.eventId(), event.userId());
        }

        if (eventReceiptRepository.findByEventIdAndConsumer(event.eventId(), EventConstants.CONSUMER_STREAK).isPresent()) {
            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[StreakService] Event already processed, skipping: eventId={}", event.eventId());
            }
            return;
        }

        try {
            StreakState initialState = StreakState.builder()
                    .userId(event.userId())
                    .currentStreak(0)
                    .build();
            streakRepository.save(initialState);
            eventReceiptRepository.save(EventReceipt.of(event.eventId(), EventConstants.CONSUMER_STREAK,
                    clock.now().toInstant(ZONE_OFFSET)));

            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[StreakService] Initial StreakState created for userId={}, eventId={}",
                        event.userId(), event.eventId());
            }
        } catch (Exception e) {
            String errorMsg = String.format("Failed to process UserCreated event: eventId=%s, userId=%s",
                    event.eventId(), event.userId());
            LOG.error("[StreakService] {}", errorMsg, e);
            throw new EventProcessingException(errorMsg, e);
        }
    }
}
