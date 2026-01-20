package com.personal.planner.domain.plan;

import com.personal.planner.domain.preference.UserPreference;
import com.personal.planner.domain.preference.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.Optional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final WeeklyPlanRepository weeklyPlanRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final com.personal.planner.domain.common.ClockProvider clock;

    /**
     * Generates a weekly plan for the NEXT week based on user preferences.
     */
    public void generateNextWeeklyPlan(String userId) {
        UserPreference prefs = preferenceRepository.findByUserId(userId)
                .orElse(UserPreference.defaultPreferences(userId));

        // Calculate the start date of the NEXT week
        // User says "Saturday 5pm generates plan from Sunday to Saturday"
        // This implies the NEXT week starts on Sunday.

        // However, user also says "start of the week is ..."
        // Let's find the upcoming startOfWeek.

        LocalDate today = clock.today(prefs.getTimeZone());
        LocalDate nextWeekStart = today.plusDays(1); // At minimum, it's tomorrow if we planning today.
        while (nextWeekStart.getDayOfWeek() != prefs.getStartOfWeek()) {
            nextWeekStart = nextWeekStart.plusDays(1);
        }

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
        }
    }

    public WeeklyPlan createWeeklyPlan(WeeklyPlan weeklyPlan) {
        return weeklyPlanRepository.save(weeklyPlan);
    }

    public Optional<WeeklyPlan> getWeeklyPlan(String userId, int weekNumber, int year) {
        return weeklyPlanRepository.findByUserAndWeek(userId, weekNumber, year);
    }

    /**
     * Materializes a DailyPlan for a specific date from the WeeklyPlan.
     * If DailyPlan already exists, it is NOT modified (idempotent).
     */
    public void materializeDay(LocalDate date, String userId) {
        if (dailyPlanRepository.findByUserIdAndDay(userId, date).isPresent()) {
            return;
        }

        int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = date.get(IsoFields.WEEK_BASED_YEAR);

        List<DailyPlan.TaskExecution> scheduledTasks = weeklyPlanRepository.findByUserAndWeek(userId, week, year)
                .map(plan -> plan.getTasksFor(date.getDayOfWeek()).stream()
                        .map(taskId -> DailyPlan.TaskExecution.builder()
                                .taskId(taskId)
                                .completed(false)
                                .build())
                        .collect(Collectors.toList()))
                .orElse(List.of());

        DailyPlan dailyPlan = DailyPlan.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .day(date)
                .closed(false)
                .tasks(scheduledTasks)
                .build();

        dailyPlanRepository.save(dailyPlan);
    }
}
