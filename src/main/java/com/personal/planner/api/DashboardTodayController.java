package com.personal.planner.api;

import com.personal.planner.domain.analytics.GoalQueryService;
import com.personal.planner.domain.goal.Goal;
import com.personal.planner.domain.goal.KeyResult;
import com.personal.planner.domain.plan.DailyPlan;
import com.personal.planner.domain.plan.DailyPlanQueryService;
import com.personal.planner.domain.streak.StreakQueryService;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for today's dashboard view.
 * Provides today's tasks, completion ratio, current streak, and active goals summary.
 * 
 * <p>All data is scoped to the authenticated user.</p>
 */
@RestController
@RequestMapping("/api/dashboard/today")
public class DashboardTodayController {

    private final DailyPlanQueryService dailyPlanQueryService;
    private final StreakQueryService streakQueryService;
    private final GoalQueryService goalQueryService;

    public DashboardTodayController(DailyPlanQueryService dailyPlanQueryService,
                                   StreakQueryService streakQueryService,
                                   GoalQueryService goalQueryService) {
        this.dailyPlanQueryService = dailyPlanQueryService;
        this.streakQueryService = streakQueryService;
        this.goalQueryService = goalQueryService;
    }

    /**
     * Gets today's dashboard data for the authenticated user.
     * Includes today's plan, completion ratio, current streak, and goal summaries.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @return today's dashboard response
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getToday(@AuthenticationPrincipal String userId) {
        DailyPlan today = dailyPlanQueryService.getToday(userId);

        int total = 0;
        int completed = 0;
        double ratio = 0.0;

        if (today != null) {
            total = today.getTasks().size();
            completed = (int) today.getTasks().stream().filter(DailyPlan.TaskExecution::isCompleted).count();
            if (total > 0) {
                ratio = (double) completed / total;
            }
        }

        List<GoalSummary> goalSummaries = goalQueryService.getActiveGoals(userId).stream()
                .map(this::mapToGoalSummary)
                .collect(Collectors.toList());

        int currentStreak = streakQueryService.getCurrent(userId) != null
                ? streakQueryService.getCurrent(userId).getCurrentStreak()
                : 0;

        DashboardResponse response = DashboardResponse.builder()
                .userId(userId)
                .todayPlan(today)
                .completionRatio(ratio)
                .currentStreak(currentStreak)
                .goalSummaries(goalSummaries)
                .build();

        return ResponseEntity.ok(response);
    }

    private GoalSummary mapToGoalSummary(Goal goal) {
        List<KeyResult> krs = goalQueryService.getKeyResults(goal.getId());
        double avgProgress = krs.isEmpty() ? 0
                : krs.stream()
                        .mapToDouble(kr -> kr.getCurrentValue() / kr.getTargetValue())
                        .average().orElse(0.0);
        return GoalSummary.builder()
                .goalId(goal.getId())
                .title(goal.getTitle())
                .averageProgress(avgProgress)
                .build();
    }

    @Data
    @Builder
    public static class DashboardResponse {
        private String userId;
        private DailyPlan todayPlan;
        private double completionRatio;
        private int currentStreak;
        private List<GoalSummary> goalSummaries;
    }

    @Data
    @Builder
    public static class GoalSummary {
        private String goalId;
        private String title;
        private double averageProgress;
    }
}
