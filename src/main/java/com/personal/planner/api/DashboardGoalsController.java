package com.personal.planner.api;

import com.personal.planner.domain.analytics.GoalQueryService;
import com.personal.planner.domain.analytics.GoalSnapshot;
import com.personal.planner.domain.analytics.TrendCalculatorService;
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
     * @return list of goal details with progress information
     */
    @GetMapping
    public ResponseEntity<List<GoalDetail>> getGoalsDashboard(@AuthenticationPrincipal String userId) {
        List<GoalDetail> goals = goalQueryService.getActiveGoals(userId).stream()
                .map(goal -> {
                    List<KeyResult> krs = goalQueryService.getKeyResults(goal.getId());
                    List<GoalSnapshot> snapshots = goalQueryService.getSnapshots(goal.getId(), 30);
                    return GoalDetail.builder()
                            .goal(goal)
                            .keyResults(krs)
                            .snapshots(snapshots)
                            .trend(trendCalculatorService.calculateTrend(snapshots))
                            .build();
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(goals);
    }

    @Data
    @Builder
    public static class GoalDetail {
        private Goal goal;
        private List<KeyResult> keyResults;
        private List<GoalSnapshot> snapshots;
        private TrendCalculatorService.Trend trend;
    }
}
