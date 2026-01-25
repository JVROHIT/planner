package com.personal.planner.api;

import com.personal.planner.domain.plan.DailyPlan;
import com.personal.planner.domain.plan.DailyPlanQueryService;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for week view dashboard.
 * Provides 7-day view with per-day completion statistics.
 * 
 * <p>All data is scoped to the authenticated user.
 * Dates are interpreted in Asia/Kolkata timezone.</p>
 */
@RestController
@RequestMapping("/api/dashboard/week")
public class DashboardWeekController {

    private final DailyPlanQueryService dailyPlanQueryService;

    public DashboardWeekController(DailyPlanQueryService dailyPlanQueryService) {
        this.dailyPlanQueryService = dailyPlanQueryService;
    }

    /**
     * Gets week view data for the authenticated user.
     * Returns completion statistics for each day in the week.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param weekStart the start date of the week (dates interpreted in Asia/Kolkata)
     * @return list of day progress for the week
     */
    @GetMapping
    public ResponseEntity<List<DayProgress>> getWeek(
            @AuthenticationPrincipal String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        List<DailyPlan> plans = dailyPlanQueryService.getWeek(userId, weekStart);
        List<DayProgress> weekProgress = plans.stream()
                .map(plan -> DayProgress.builder()
                        .date(plan.getDay())
                        .totalTasks(plan.getTasks().size())
                        .completedTasks(
                                (int) plan.getTasks().stream().filter(DailyPlan.TaskExecution::isCompleted).count())
                        .closed(plan.isClosed())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(weekProgress);
    }

    @Data
    @Builder
    public static class DayProgress {
        private LocalDate date;
        private int totalTasks;
        private int completedTasks;
        private boolean closed;
    }
}
