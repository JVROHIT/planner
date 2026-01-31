package com.personal.planner.api;

import com.personal.planner.domain.analytics.GoalQueryService;
import com.personal.planner.domain.analytics.GoalSnapshot;
import com.personal.planner.domain.analytics.TrendCalculatorService;
import com.personal.planner.domain.common.constants.AnalyticsConstants;
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
    private final TrendCalculatorService trendCalculatorService;

    public DashboardTodayController(DailyPlanQueryService dailyPlanQueryService,
                                   StreakQueryService streakQueryService,
                                   GoalQueryService goalQueryService,
                                   TrendCalculatorService trendCalculatorService) {
        this.dailyPlanQueryService = dailyPlanQueryService;
        this.streakQueryService = streakQueryService;
        this.goalQueryService = goalQueryService;
        this.trendCalculatorService = trendCalculatorService;
    }

    /**
     * Gets today's dashboard data for the authenticated user.
     * Includes today's plan, completion ratio, current streak, and goal summaries.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @return today's dashboard response wrapped in ApiResponse
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getToday(@AuthenticationPrincipal String userId) {
        DailyPlan today = dailyPlanQueryService.getToday(userId);

        int total = 0;
        int completed = 0;
        double ratio = 0.0;

        if (today != null) {
            total = today.getEntries().size();
            completed = (int) today.getEntries().stream()
                    .filter(entry -> entry.getStatus() == DailyPlan.Status.COMPLETED)
                    .count();
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

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private GoalSummary mapToGoalSummary(Goal goal) {
        List<KeyResult> krs = goalQueryService.getKeyResults(goal.getId());
        List<GoalSnapshot> snapshots = goalQueryService.getSnapshots(goal.getId(), 30);
        GoalSnapshot latestSnapshot = snapshots.isEmpty() ? null : snapshots.get(0);
        double actual = latestSnapshot != null ? latestSnapshot.getActual() : 0;
        double expected = latestSnapshot != null ? latestSnapshot.getExpected() : 0;
        double avgProgress = krs.isEmpty() ? 0
                : krs.stream()
                        .mapToDouble(kr -> kr.getProgress())
                        .average().orElse(0.0);
        return GoalSummary.builder()
                .goalId(goal.getId())
                .title(goal.getTitle())
                .averageProgress(avgProgress)
                .status(determineStatus(actual, expected))
                .trend(trendCalculatorService.calculateTrend(snapshots))
                .build();
    }

    private ProgressStatus determineStatus(double actual, double expected) {
        double delta = actual - expected;
        if (delta > AnalyticsConstants.TREND_THRESHOLD) {
            return ProgressStatus.AHEAD;
        }
        if (delta < -AnalyticsConstants.TREND_THRESHOLD) {
            return ProgressStatus.BEHIND;
        }
        return ProgressStatus.ON_TRACK;
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
        private ProgressStatus status;
        private TrendCalculatorService.Trend trend;
    }

    public enum ProgressStatus {
        AHEAD,
        ON_TRACK,
        BEHIND
    }
}
