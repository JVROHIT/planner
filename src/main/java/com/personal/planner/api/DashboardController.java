package com.personal.planner.api;

import com.personal.planner.domain.analytics.GoalQueryService;
import com.personal.planner.domain.analytics.GoalSnapshot;
import com.personal.planner.domain.analytics.TrendCalculatorService;
import com.personal.planner.domain.goal.Goal;
import com.personal.planner.domain.goal.KeyResult;
import com.personal.planner.domain.plan.DailyPlan;
import com.personal.planner.domain.plan.DailyPlanQueryService;
import com.personal.planner.domain.streak.StreakQueryService;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * High-level read model for the FocusFlow dashboards. Identity-scoped via JWT.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DailyPlanQueryService dailyPlanQueryService;
    private final StreakQueryService streakQueryService;
    private final GoalQueryService goalQueryService;
    private final TrendCalculatorService trendCalculatorService;

    public DashboardController(DailyPlanQueryService dailyPlanQueryService,
            StreakQueryService streakQueryService,
            GoalQueryService goalQueryService,
            TrendCalculatorService trendCalculatorService) {
        this.dailyPlanQueryService = dailyPlanQueryService;
        this.streakQueryService = streakQueryService;
        this.goalQueryService = goalQueryService;
        this.trendCalculatorService = trendCalculatorService;
    }

    @GetMapping("/today")
    public ResponseEntity<DashboardResponse> getToday() {
        String userId = getUserId();
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

        DashboardResponse response = DashboardResponse.builder()
                .userId(userId)
                .todayPlan(today)
                .completionRatio(ratio)
                .currentStreak(streakQueryService.getCurrent(userId) != null
                        ? streakQueryService.getCurrent(userId).getCurrentStreak()
                        : 0)
                .goalSummaries(goalSummaries)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/week")
    public ResponseEntity<?> getWeek(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        List<DailyPlan> plans = dailyPlanQueryService.getWeek(getUserId(), weekStart);
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

    @GetMapping("/goals")
    public ResponseEntity<?> getGoalsDashboard() {
        String userId = getUserId();
        List<GoalDetail> goals = goalQueryService.getActiveGoals(userId).stream()
                .map(goal -> {
                    List<KeyResult> krs = goalQueryService.getKeyResults(goal.getId());
                    List<GoalSnapshot> snapshots = goalQueryService.getSnapshots(goal.getId(), 30);
                    return GoalDetail.builder()
                            .goal(goal)
                            .keyResults(krs)
                            .snapshots(snapshots)
                            .trend(trendCalculatorService.calculateTrend(snapshots, 7))
                            .build();
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(goals);
    }

    private GoalSummary mapToGoalSummary(Goal goal) {
        List<KeyResult> krs = goalQueryService.getKeyResults(goal.getId());
        double avgProgress = krs.isEmpty() ? 0
                : krs.stream()
                        .mapToDouble(kr -> kr.getCurrentProgress() / kr.getTargetValue())
                        .average().orElse(0.0);
        return GoalSummary.builder()
                .goalId(goal.getId())
                .title(goal.getTitle())
                .averageProgress(avgProgress)
                .build();
    }

    private String getUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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

    @Data
    @Builder
    public static class DayProgress {
        private LocalDate date;
        private int totalTasks;
        private int completedTasks;
        private boolean closed;
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
