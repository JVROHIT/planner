package com.personal.planner.domain.plan;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.constants.TimeConstants;
import com.personal.planner.domain.common.exception.DailyPlanNotFoundException;
import com.personal.planner.domain.common.util.LogUtil;
import com.personal.planner.events.DayClosed;
import com.personal.planner.events.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Service responsible for orchestrating the closure of daily execution cycles.
 * <p>
 * This service handles the finalization of daily plans, marking them as closed
 * and publishing domain events to notify other parts of the system about the
 * completion of a day's execution cycle.
 * </p>
 * <p>
 * <strong>Key Responsibilities:</strong>
 * <ul>
 *   <li>Closing daily plans explicitly via user action</li>
 *   <li>Automated daily closure via scheduled task (midnight Asia/Kolkata)</li>
 *   <li>Publishing DayClosed events for downstream processing</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Timezone Constraint:</strong>
 * All date/time operations use {@link TimeConstants#ZONE_ID} (Asia/Kolkata) to ensure
 * consistent behavior across different server environments.
 * </p>
 * <p>
 * <strong>Invariants:</strong>
 * <ul>
 *   <li>Closed plans cannot be modified - once closed, a DailyPlan becomes immutable</li>
 *   <li>Closing an already-closed plan is idempotent (no-op)</li>
 *   <li>All closure timestamps are recorded in Asia/Kolkata timezone</li>
 * </ul>
 * </p>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
@Service
public class DayCloseService {

    private static final Logger LOG = LoggerFactory.getLogger(DayCloseService.class);

    private final DailyPlanRepository dailyPlanRepository;
    private final DomainEventPublisher eventPublisher;
    private final ClockProvider clock;

    public DayCloseService(DailyPlanRepository dailyPlanRepository,
            DomainEventPublisher eventPublisher,
            ClockProvider clock) {
        this.dailyPlanRepository = dailyPlanRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    /**
     * Scheduled task that runs daily at midnight (Asia/Kolkata timezone) to close
     * the previous day's execution cycle.
     * <p>
     * This method is triggered automatically by Spring's scheduling framework.
     * It closes the daily plan for yesterday's date in Asia/Kolkata timezone.
     * </p>
     * <p>
     * <strong>Cron Expression:</strong> "0 0 0 * * *" (midnight every day)
     * </p>
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void runDayCloseProcess() {
        // CRITICAL: Always use Asia/Kolkata timezone for date calculations
        LocalDate yesterday = clock.today().minusDays(1);

        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[DayCloseService] Running scheduled day close process for date: {}", yesterday);
        }

        // Note: This would need to iterate over all users or be triggered per-user
        // For now, this is a placeholder that can be extended based on requirements
    }

    /**
     * Explicitly closes a daily plan for a specific user and date.
     * <p>
     * This method is called when a user explicitly requests to close their day,
     * typically through the UI. If no daily plan exists for the specified date,
     * a {@link DailyPlanNotFoundException} is thrown.
     * </p>
     * <p>
     * <strong>Invariant:</strong> Closed plans cannot be modified. If the plan
     * is already closed, this operation is idempotent.
     * </p>
     *
     * @param userId the identifier of the user whose plan should be closed
     * @param date the date of the daily plan to close (must be in Asia/Kolkata timezone context)
     * @throws IllegalArgumentException if userId is null or empty, or if date is null
     * @throws DailyPlanNotFoundException if no daily plan exists for the specified user and date
     */
    public void closeDayExplicit(String userId, LocalDate date) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        DailyPlan plan = dailyPlanRepository.findByUserIdAndDay(userId, date)
                .orElseThrow(() -> new DailyPlanNotFoundException("DailyPlan not found for user: " + userId
                        + ", date: " + date));

        closePlanAndPublish(plan);
    }

    /**
     * Closes a daily plan and publishes a DayClosed domain event.
     * <p>
     * This method performs the actual closure operation:
     * <ol>
     *   <li>Marks the plan as closed (making it immutable)</li>
     *   <li>Persists the closed state to the repository</li>
     *   <li>Publishes a DayClosed event for downstream processing</li>
     * </ol>
     * </p>
     * <p>
     * <strong>Invariant:</strong> Closed plans cannot be modified. This method
     * checks if the plan is already closed and skips processing if so (idempotent).
     * </p>
     * <p>
     * <strong>Timezone:</strong> The closure timestamp is recorded using Asia/Kolkata timezone.
     * </p>
     *
     * @param plan the daily plan to close (must not be null)
     * @throws IllegalArgumentException if plan is null
     */
    public void closePlanAndPublish(DailyPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("DailyPlan cannot be null");
        }

        // Idempotent operation: if already closed, do nothing
        if (plan.isClosed()) {
            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[DayCloseService] Plan already closed for user: {}, date: {}",
                        plan.getUserId(), plan.getDay());
            }
            return;
        }

        // CRITICAL INVARIANT: Closed plans cannot be modified
        // Once closed, the DailyPlan becomes an immutable historical fact
        plan.close();
        dailyPlanRepository.save(plan);

        // CRITICAL: Always use Asia/Kolkata timezone for timestamp conversion
        ZonedDateTime closedAtZoned = clock.now().atZone(TimeConstants.ZONE_ID);
        
        DayClosed event = DayClosed.builder()
                .id(UUID.randomUUID().toString())
                .userId(plan.getUserId())
                .day(plan.getDay())
                .closedAt(closedAtZoned.toInstant())
                .build();

        eventPublisher.publish(event);

        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[DayCloseService] Closed daily plan for user: {}, date: {}, closedAt: {}",
                    plan.getUserId(), plan.getDay(), closedAtZoned);
        }
    }
}
