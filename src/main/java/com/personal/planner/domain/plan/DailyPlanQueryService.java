package com.personal.planner.domain.plan;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.constants.TimeConstants;
import com.personal.planner.domain.common.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Query service for retrieving daily plan data for UI and dashboards.
 * <p>
 * This service provides read-only access to daily plan information. It follows the
 * CQRS pattern, separating read operations from write operations handled by
 * {@link PlanningService}.
 * </p>
 * <p>
 * <strong>Key Responsibilities:</strong>
 * <ul>
 *   <li>Retrieving today's daily plan (with auto-materialization if missing)</li>
 *   <li>Retrieving daily plans for specific dates</li>
 *   <li>Retrieving weekly collections of daily plans</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Timezone Constraint:</strong>
 * All date operations use {@link TimeConstants#ZONE_ID} (Asia/Kolkata) to ensure
 * consistent behavior across different server environments.
 * </p>
 * <p>
 * <strong>Invariants:</strong>
 * <ul>
 *   <li>This service never modifies domain state - it is read-only</li>
 *   <li>Auto-materialization ensures today's plan always exists when queried</li>
 *   <li>All dates are interpreted in Asia/Kolkata timezone context</li>
 * </ul>
 * </p>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
@Service
public class DailyPlanQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyPlanQueryService.class);

    private static final int DAYS_IN_WEEK = 7;

    private final DailyPlanRepository dailyPlanRepository;
    private final PlanningService planningService;
    private final ClockProvider clock;

    public DailyPlanQueryService(DailyPlanRepository dailyPlanRepository,
            PlanningService planningService,
            ClockProvider clock) {
        this.dailyPlanRepository = dailyPlanRepository;
        this.planningService = planningService;
        this.clock = clock;
    }

    /**
     * Retrieves today's execution truth (daily plan) for the specified user.
     * <p>
     * This method automatically materializes a daily plan if one does not exist
     * for today's date. The materialization process creates a daily plan from the
     * corresponding weekly plan.
     * </p>
     * <p>
     * <strong>Timezone:</strong> "Today" is determined using Asia/Kolkata timezone
     * via {@link ClockProvider#today()}.
     * </p>
     * <p>
     * <strong>Auto-materialization:</strong> If no daily plan exists for today,
     * this method will create one by calling {@link PlanningService#materializeDay(LocalDate, String)}.
     * </p>
     *
     * @param userId the identifier of the user whose daily plan should be retrieved
     * @return the daily plan for today, or null if materialization fails
     * @throws IllegalArgumentException if userId is null or empty
     */
    public DailyPlan getToday(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        // CRITICAL: Always use Asia/Kolkata timezone for "today"
        LocalDate today = clock.today();

        return dailyPlanRepository.findByUserIdAndDay(userId, today)
                .orElseGet(() -> {
                    if (LogUtil.isDebugEnabled()) {
                        LOG.debug("[DailyPlanQueryService] Auto-materializing daily plan for user: {}, date: {}",
                                userId, today);
                    }
                    planningService.materializeDay(today, userId);
                    return dailyPlanRepository.findByUserIdAndDay(userId, today)
                            .orElse(null);
                });
    }

    /**
     * Retrieves a daily plan for a specific user and date.
     * <p>
     * Returns null if no daily plan exists for the specified date. Unlike
     * {@link #getToday(String)}, this method does not auto-materialize plans.
     * </p>
     * <p>
     * <strong>Timezone:</strong> The date parameter should be interpreted in
     * Asia/Kolkata timezone context.
     * </p>
     *
     * @param userId the identifier of the user whose daily plan should be retrieved
     * @param date the date for which to retrieve the daily plan
     * @return the daily plan for the specified date, or null if not found
     * @throws IllegalArgumentException if userId is null or empty, or if date is null
     */
    public DailyPlan getDay(String userId, LocalDate date) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        return dailyPlanRepository.findByUserIdAndDay(userId, date).orElse(null);
    }

    /**
     * Retrieves all daily plans for a week starting from the specified date.
     * <p>
     * This method collects daily plans for seven consecutive days starting from
     * the provided weekStart date. Only plans that exist are included in the result
     * (missing days are skipped, not materialized).
     * </p>
     * <p>
     * <strong>Week Calculation:</strong> Uses simple date arithmetic to iterate through
     * seven consecutive days. The weekStart parameter should represent the first day
     * of the week according to the user's preference (e.g., Monday or Sunday).
     * </p>
     * <p>
     * <strong>Timezone:</strong> The weekStart date should be in Asia/Kolkata timezone context.
     * </p>
     *
     * @param userId the identifier of the user whose weekly plans should be retrieved
     * @param weekStart the first day of the week (typically Monday or Sunday based on user preference)
     * @return a list of daily plans for the week (may contain fewer than 7 plans if some days are missing)
     * @throws IllegalArgumentException if userId is null or empty, or if weekStart is null
     */
    public List<DailyPlan> getWeek(String userId, LocalDate weekStart) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        if (weekStart == null) {
            throw new IllegalArgumentException("WeekStart date cannot be null");
        }

        List<DailyPlan> plans = new ArrayList<>();
        for (int i = 0; i < DAYS_IN_WEEK; i++) {
            LocalDate currentDate = weekStart.plusDays(i);
            dailyPlanRepository.findByUserIdAndDay(userId, currentDate)
                    .ifPresent(plans::add);
        }

        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[DailyPlanQueryService] Retrieved {} daily plans for user: {}, week starting: {}",
                    plans.size(), userId, weekStart);
        }

        return plans;
    }
}
