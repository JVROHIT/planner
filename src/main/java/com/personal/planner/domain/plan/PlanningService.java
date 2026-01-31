package com.personal.planner.domain.plan;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.util.LogUtil;
import com.personal.planner.domain.preference.UserPreference;
import com.personal.planner.domain.preference.UserPreferenceRepository;
import com.personal.planner.domain.task.Task;
import com.personal.planner.domain.task.TaskRepository;
import com.personal.planner.domain.user.UserTimeZoneService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
 *   <li>Retrieving weekly plans by user and week start date</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Timezone Constraint:</strong>
 * All date operations use the user's timezone, defaulting to Asia/Kolkata.
 * </p>
 * <p>
 * <strong>Invariants:</strong>
 * <ul>
 *   <li>Daily plans are idempotent - materializing an existing plan does not modify it</li>
 *   <li>Weekly plans are identified by user ID and week start date (unique combination)</li>
 *   <li>All temporal calculations use the user's timezone (default Asia/Kolkata)</li>
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
    private final TaskRepository taskRepository;
    private final UserTimeZoneService timeZoneService;
    private final ClockProvider clock;

    /**
     * Generates a weekly plan for the NEXT week based on user preferences.
     * <p>
     * Calculates the start date of the next week using the user's preferred start of week
     * (e.g., Monday, Sunday). If a weekly plan already exists for that week, no new plan
     * is created (idempotent operation).
     * </p>
     * <p>
     * <strong>Timezone:</strong> All date calculations use the user's timezone.
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

        LocalDate today = clock.today(timeZoneService.resolveZone(userId));
        LocalDate nextWeekStart = calculateNextWeekStart(today, prefs.getStartOfWeek());

        Optional<WeeklyPlan> existing = weeklyPlanRepository.findByUserAndWeekStart(userId, nextWeekStart);
        if (existing.isEmpty()) {
            WeeklyPlan plan = WeeklyPlan.builder()
                    .userId(userId)
                    .weekStart(nextWeekStart)
                    .updatedAt(clock.nowInstant())
                    .build();
            weeklyPlanRepository.save(plan);

            if (LogUtil.isDebugEnabled()) {
                LOG.debug("[PlanningService] Created weekly plan for user: {}, weekStart: {}", userId, nextWeekStart);
            }
        }
    }

    /**
     * Creates or updates a weekly plan.
     * <p>
     * This method is used for explicit weekly plan creation or updates. If a plan already
     * exists for the user/weekStart, it is updated in place.
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
            LOG.debug("[PlanningService] Saving weekly plan for user: {}, weekStart: {}",
                    weeklyPlan.getUserId(), weeklyPlan.getWeekStart());
        }

        Optional<WeeklyPlan> existing = weeklyPlanRepository.findByUserAndWeekStart(
                weeklyPlan.getUserId(), weeklyPlan.getWeekStart());
        if (existing.isPresent()) {
            WeeklyPlan current = existing.get();
            current.setTaskGrid(weeklyPlan.getTaskGrid());
            current.setUpdatedAt(clock.nowInstant());
            return weeklyPlanRepository.save(current);
        }

        weeklyPlan.setUpdatedAt(clock.nowInstant());
        return weeklyPlanRepository.save(weeklyPlan);
    }

    /**
     * Retrieves a weekly plan by user ID and week start date.
     * <p>
     * Returns an empty Optional if no plan exists for the specified criteria.
     * </p>
     *
     * @param userId the identifier of the user who owns the plan
     * @param weekStart the week start date
     * @return an Optional containing the weekly plan if found, empty otherwise
     * @throws IllegalArgumentException if userId is null or empty, or if weekStart is null
     */
    public Optional<WeeklyPlan> getWeeklyPlan(String userId, LocalDate weekStart) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        if (weekStart == null) {
            throw new IllegalArgumentException("Week start date cannot be null");
        }

        return weeklyPlanRepository.findByUserAndWeekStart(userId, weekStart);
    }

    /**
     * Retrieves a weekly plan for a specific date based on the user's start-of-week preference.
     * If no weekly plan exists for the computed weekStart, an empty plan is created automatically.
     *
     * @param userId the identifier of the user who owns the plan
     * @param date any date within the desired week
     * @return an Optional containing the weekly plan if found, empty otherwise
     */
    public Optional<WeeklyPlan> getWeeklyPlanForDate(String userId, LocalDate date) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        UserPreference prefs = preferenceRepository.findByUserId(userId)
                .orElse(UserPreference.defaultPreferences(userId));
        LocalDate weekStart = calculateWeekStart(date, prefs.getStartOfWeek());
        WeeklyPlan weeklyPlan = weeklyPlanRepository.findByUserAndWeekStart(userId, weekStart)
                .orElseGet(() -> {
                    WeeklyPlan plan = WeeklyPlan.builder()
                            .userId(userId)
                            .weekStart(weekStart)
                            .updatedAt(clock.nowInstant())
                            .build();
                    weeklyPlanRepository.save(plan);
                    if (LogUtil.isDebugEnabled()) {
                        LOG.debug("[PlanningService] Auto-created weekly plan for user: {}, weekStart: {}",
                                userId, weekStart);
                    }
                    return plan;
                });
        return Optional.of(weeklyPlan);
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
     * <strong>Weekly plan availability:</strong> If no weekly plan exists for the week,
     * this method will create an empty weekly plan automatically to preserve execution continuity.
     * </p>
     * <p>
     * <strong>Timezone:</strong> The date parameter should be in Asia/Kolkata timezone context.
     * </p>
     *
     * @param date the date for which to materialize the daily plan
     * @param userId the identifier of the user who owns the plan
     * @throws IllegalArgumentException if date or userId is null
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

        UserPreference prefs = preferenceRepository.findByUserId(userId)
                .orElse(UserPreference.defaultPreferences(userId));
        LocalDate weekStart = calculateWeekStart(date, prefs.getStartOfWeek());

        WeeklyPlan weeklyPlan = weeklyPlanRepository.findByUserAndWeekStart(userId, weekStart)
                .orElseGet(() -> {
                    WeeklyPlan plan = WeeklyPlan.builder()
                            .userId(userId)
                            .weekStart(weekStart)
                            .updatedAt(clock.nowInstant())
                            .build();
                    weeklyPlanRepository.save(plan);
                    if (LogUtil.isDebugEnabled()) {
                        LOG.debug("[PlanningService] Auto-created weekly plan for user: {}, weekStart: {}",
                                userId, weekStart);
                    }
                    return plan;
                });

        List<DailyPlan.Entry> scheduledTasks = extractScheduledTasks(weeklyPlan, date);

        DailyPlan dailyPlan = DailyPlan.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .day(date)
                .closed(false)
                .entries(scheduledTasks)
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
     * Converts task IDs from the weekly plan into DailyPlan entries with status
     * set to PENDING (initial state).
     * </p>
     *
     * @param weeklyPlan the weekly plan containing the task grid
     * @param dayOfWeek the day of week for which to extract tasks
     * @return a list of DailyPlan entries representing scheduled tasks
     */
    private List<DailyPlan.Entry> extractScheduledTasks(WeeklyPlan weeklyPlan, LocalDate date) {
        return weeklyPlan.getTasksFor(date).stream()
                .map(taskId -> {
                    String title = taskRepository.findById(taskId)
                            .map(Task::getTitle)
                            .orElse(null);
                    return DailyPlan.Entry.builder()
                            .taskId(taskId)
                            .title(title)
                            .status(DailyPlan.Status.PENDING)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private LocalDate calculateWeekStart(LocalDate date, DayOfWeek startOfWeek) {
        return date.with(TemporalAdjusters.previousOrSame(startOfWeek));
    }
}
