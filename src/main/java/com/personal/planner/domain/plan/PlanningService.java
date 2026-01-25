package com.personal.planner.domain.plan;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.constants.TimeConstants;
import com.personal.planner.domain.common.exception.WeeklyPlanNotFoundException;
import com.personal.planner.domain.common.util.LogUtil;
import com.personal.planner.domain.preference.UserPreference;
import com.personal.planner.domain.preference.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for planning operations in the FocusFlow domain.
 * <p>
 * This service handles the creation and management of weekly and daily plans.
 * It orchestrates the materialization of daily execution plans from weekly intent plans.
 * </p>
 * <p>
 * <strong>Key Responsibilities:</strong>
 * <ul>
 *   <li>Generating weekly plans for upcoming weeks based on user preferences</li>
 *   <li>Creating and persisting weekly plan entities</li>
 *   <li>Materializing daily plans from weekly plans (idempotent operation)</li>
 *   <li>Retrieving weekly plans by user, week number, and year</li>
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
 *   <li>Daily plans are idempotent - materializing an existing plan does not modify it</li>
 *   <li>Weekly plans are identified by user ID, week number, and year (unique combination)</li>
 *   <li>All temporal calculations use Asia/Kolkata timezone</li>
 * </ul>
 * </p>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class PlanningService {

    private static final Logger LOG = LoggerFactory.getLogger(PlanningService.class);

    private final WeeklyPlanRepository weeklyPlanRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final ClockProvider clock;

    /**
     * Generates a weekly plan for the NEXT week based on user preferences.
     * <p>
     * Calculates the start date of the next week using the user's preferred start of week
     * (e.g., Monday, Sunday). If a weekly plan already exists for that week, no new plan
     * is created (idempotent operation).
     * </p>
     * <p>
     * <strong>Timezone:</strong> All date calculations use Asia/Kolkata timezone.
     * </p>
     *
     * @param userId the identifier of the user for whom to generate the plan
     * @throws IllegalArgumentException if userId is null or empty
     */
    public void generateNextWeeklyPlan(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[PlanningService] Generating next weekly plan for user: {}", userId);
        }

        UserPreference prefs = preferenceRepository.findByUserId(userId)
                .orElse(UserPreference.defaultPreferences(userId));

        // CRITICAL: Always use Asia/Kolkata timezone, never user preference timezone
        LocalDate today = clock.today();
        LocalDate nextWeekStart = calculateNextWeekStart(today, prefs.getStartOfWeek());

        int week = nextWeekStart.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = nextWeekStart.get(IsoFields.WEEK_BASED_YEAR);

        Optional<WeeklyPlan> existing = weeklyPlanRepository.findByUserAndWeek(userId, week, year);
        if (existing.isEmpty()) {
            WeeklyPlan plan = WeeklyPlan.builder()
                    .userId(userId)
                    .weekNumber(week)
                    .year(year)
                    .build();
            weeklyPlanRepository.save(plan);

            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[PlanningService] Created weekly plan for user: {}, week: {}, year: {}", userId, week, year);
            }
        }
    }

    /**
     * Creates and persists a new weekly plan.
     * <p>
     * This method is used for explicit weekly plan creation. The plan is saved to the repository
     * and returned with its generated identifier.
     * </p>
     *
     * @param weeklyPlan the weekly plan entity to create
     * @return the persisted weekly plan with its generated identifier
     * @throws IllegalArgumentException if weeklyPlan is null
     */
    public WeeklyPlan createWeeklyPlan(WeeklyPlan weeklyPlan) {
        if (weeklyPlan == null) {
            throw new IllegalArgumentException("WeeklyPlan cannot be null");
        }

        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[PlanningService] Creating weekly plan for user: {}, week: {}, year: {}",
                    weeklyPlan.getUserId(), weeklyPlan.getWeekNumber(), weeklyPlan.getYear());
        }

        return weeklyPlanRepository.save(weeklyPlan);
    }

    /**
     * Retrieves a weekly plan by user ID, week number, and year.
     * <p>
     * Returns an empty Optional if no plan exists for the specified criteria.
     * </p>
     *
     * @param userId the identifier of the user who owns the plan
     * @param weekNumber the week number (1-52/53) within the year
     * @param year the ISO week-based year
     * @return an Optional containing the weekly plan if found, empty otherwise
     * @throws IllegalArgumentException if userId is null or empty, or if weekNumber/year are invalid
     */
    public Optional<WeeklyPlan> getWeeklyPlan(String userId, int weekNumber, int year) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        if (weekNumber < 1 || weekNumber > 53) {
            throw new IllegalArgumentException("Week number must be between 1 and 53");
        }

        return weeklyPlanRepository.findByUserAndWeek(userId, weekNumber, year);
    }

    /**
     * Materializes a DailyPlan for a specific date from the WeeklyPlan.
     * <p>
     * This method creates a daily execution plan by extracting tasks scheduled for the given
     * day from the corresponding weekly plan. The operation is idempotent - if a daily plan
     * already exists for the specified date, it is not modified.
     * </p>
     * <p>
     * <strong>Invariant:</strong> Daily plans are immutable once created. If a plan already
     * exists, this method returns without modification.
     * </p>
     * <p>
     * <strong>Timezone:</strong> The date parameter should be in Asia/Kolkata timezone context.
     * </p>
     *
     * @param date the date for which to materialize the daily plan
     * @param userId the identifier of the user who owns the plan
     * @throws IllegalArgumentException if date or userId is null
     * @throws WeeklyPlanNotFoundException if no weekly plan exists for the week containing the date
     */
    public void materializeDay(LocalDate date, String userId) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        // Idempotent operation: if daily plan already exists, do not modify it
        if (dailyPlanRepository.findByUserIdAndDay(userId, date).isPresent()) {
            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[PlanningService] Daily plan already exists for user: {}, date: {}", userId, date);
            }
            return;
        }

        int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = date.get(IsoFields.WEEK_BASED_YEAR);

        WeeklyPlan weeklyPlan = weeklyPlanRepository.findByUserAndWeek(userId, week, year)
                .orElseThrow(() -> new WeeklyPlanNotFoundException("WeeklyPlan not found for user: " + userId
                        + ", week: " + week + ", year: " + year));

        List<DailyPlan.TaskExecution> scheduledTasks = extractScheduledTasks(weeklyPlan, date.getDayOfWeek());

        DailyPlan dailyPlan = DailyPlan.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .day(date)
                .closed(false)
                .tasks(scheduledTasks)
                .build();

        dailyPlanRepository.save(dailyPlan);

        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[PlanningService] Materialized daily plan for user: {}, date: {}, tasks: {}",
                    userId, date, scheduledTasks.size());
        }
    }

    /**
     * Calculates the next week start date based on user preferences.
     * <p>
     * Uses {@link TemporalAdjusters#next(DayOfWeek)} to find the next occurrence of the
     * specified day of week, ensuring we always get a date in the future (or today if today
     * matches the start of week).
     * </p>
     * <p>
     * <strong>Timezone:</strong> All calculations use Asia/Kolkata timezone via {@link ClockProvider}.
     * </p>
     *
     * @param currentDate the reference date (typically today in Asia/Kolkata timezone)
     * @param startOfWeek the user's preferred start of week (e.g., DayOfWeek.MONDAY)
     * @return the next week's start date (or today if today matches startOfWeek)
     */
    private LocalDate calculateNextWeekStart(LocalDate currentDate, DayOfWeek startOfWeek) {
        // Use TemporalAdjusters instead of while loop for better performance and clarity
        LocalDate nextStart = currentDate.with(TemporalAdjusters.nextOrSame(startOfWeek));
        
        // If today is the start of week, we want NEXT week's start, not this week's
        if (nextStart.equals(currentDate)) {
            nextStart = currentDate.with(TemporalAdjusters.next(startOfWeek));
        }
        
        return nextStart;
    }

    /**
     * Extracts scheduled tasks from a weekly plan for a specific day of week.
     * <p>
     * Converts task IDs from the weekly plan into TaskExecution entities with completed
     * status set to false (initial state).
     * </p>
     *
     * @param weeklyPlan the weekly plan containing the task grid
     * @param dayOfWeek the day of week for which to extract tasks
     * @return a list of TaskExecution entities representing scheduled tasks
     */
    private List<DailyPlan.TaskExecution> extractScheduledTasks(WeeklyPlan weeklyPlan, DayOfWeek dayOfWeek) {
        return weeklyPlan.getTasksFor(dayOfWeek).stream()
                .map(taskId -> DailyPlan.TaskExecution.builder()
                        .taskId(taskId)
                        .completed(false)
                        .build())
                .collect(Collectors.toList());
    }
}
