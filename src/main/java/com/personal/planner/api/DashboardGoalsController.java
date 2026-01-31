package com.personal.planner.api;

import com.personal.planner.domain.analytics.GoalQueryService;
import com.personal.planner.domain.analytics.GoalSnapshot;
import com.personal.planner.domain.analytics.TrendCalculatorService;
import com.personal.planner.domain.common.constants.AnalyticsConstants;
import com.personal.planner.domain.goal.Goal;
import com.personal.planner.domain.goal.KeyResult;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for goals dashboard view.
 * Provides detailed goal progress with snapshots and trends.
 * 
 * <p>All data is scoped to the authenticated user.</p>
 */
@RestController
@RequestMapping("/api/dashboard/goals")
public class DashboardGoalsController {

    private final GoalQueryService goalQueryService;
    private final TrendCalculatorService trendCalculatorService;

    public DashboardGoalsController(GoalQueryService goalQueryService,
                                    TrendCalculatorService trendCalculatorService) {
        this.goalQueryService = goalQueryService;
        this.trendCalculatorService = trendCalculatorService;
    }

    /**
     * Gets detailed goals dashboard data for the authenticated user.
     * Includes goal details, key results, snapshots, and trends.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @return list of goal details with progress information wrapped in ApiResponse
     */
    @GetMapping
    public ResponseEntity<ApiResponse<GoalsDashboard>> getGoalsDashboard(@AuthenticationPrincipal String userId) {
        List<GoalDetail> goals = goalQueryService.getActiveGoals(userId).stream()
                .map(goal -> {
                    List<KeyResult> krs = goalQueryService.getKeyResults(goal.getId());
                    List<GoalSnapshot> snapshots = goalQueryService.getSnapshots(goal.getId(), 30);
                    GoalSnapshot latestSnapshot = snapshots.isEmpty() ? null : snapshots.get(0);
                    double actual = latestSnapshot != null ? latestSnapshot.getActual() : 0;
                    double expected = latestSnapshot != null ? latestSnapshot.getExpected() : 0;
                    ProgressStatus status = determineStatus(actual, expected);
                    return GoalDetail.builder()
                            .goal(goal)
                            .keyResults(krs)
                            .trend(trendCalculatorService.calculateTrend(snapshots))
                            .latestSnapshot(latestSnapshot)
                            .status(status)
                            .actualPercent(actual * 100)
                            .expectedPercent(expected * 100)
                            .build();
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(
                GoalsDashboard.builder().goals(goals).build()));
    }

    @Data
    @Builder
    public static class GoalsDashboard {
        private List<GoalDetail> goals;
    }

    @Data
    @Builder
    public static class GoalDetail {
        private Goal goal;
        private List<KeyResult> keyResults;
        private GoalSnapshot latestSnapshot;
        private ProgressStatus status;
        private TrendCalculatorService.Trend trend;
        private double actualPercent;
        private double expectedPercent;
    }

    public enum ProgressStatus {
        AHEAD,
        ON_TRACK,
        BEHIND
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
}
