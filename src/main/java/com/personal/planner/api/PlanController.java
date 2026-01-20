package com.personal.planner.api;

import com.personal.planner.domain.plan.DailyPlanQueryService;
import com.personal.planner.domain.plan.DayCloseService;
import com.personal.planner.domain.plan.PlanningService;
import com.personal.planner.domain.plan.WeeklyPlan;
import com.personal.planner.domain.task.TaskService;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing weekly and daily plans. Identity-scoped via JWT.
 */
@RestController
@RequestMapping("/api")
public class PlanController {

    private final PlanningService planningService;
    private final TaskService taskService;
    private final DailyPlanQueryService dailyPlanQueryService;
    private final DayCloseService dayCloseService;

    public PlanController(PlanningService planningService,
            TaskService taskService,
            DailyPlanQueryService dailyPlanQueryService,
            DayCloseService dayCloseService) {
        this.planningService = planningService;
        this.taskService = taskService;
        this.dailyPlanQueryService = dailyPlanQueryService;
        this.dayCloseService = dayCloseService;
    }

    @PostMapping("/weekly-plan")
    public ResponseEntity<?> createWeeklyPlan(@RequestBody WeeklyPlanRequest request) {
        WeeklyPlan plan = WeeklyPlan.builder()
                .userId(getUserId())
                .weekNumber(request.weekNumber)
                .year(request.year)
                .taskGrid(request.taskGrid)
                .build();
        return ResponseEntity.ok(planningService.createWeeklyPlan(plan));
    }

    @GetMapping("/weekly-plan/{date}")
    public ResponseEntity<?> getWeeklyPlan(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        int weekNumber = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = date.get(IsoFields.WEEK_BASED_YEAR);
        return planningService.getWeeklyPlan(getUserId(), weekNumber, year)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/daily/today")
    public ResponseEntity<?> getToday() {
        return ResponseEntity.ok(dailyPlanQueryService.getToday(getUserId()));
    }

    @GetMapping("/daily/{date}")
    public ResponseEntity<?> getDay(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(dailyPlanQueryService.getDay(getUserId(), date));
    }

    @PostMapping("/daily/{date}/tasks/{taskId}/complete")
    public ResponseEntity<?> completeTask(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String taskId) {
        taskService.completeTask(taskId, date, getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/daily/{date}/tasks/{taskId}/miss")
    public ResponseEntity<?> missTask(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String taskId) {
        taskService.missTask(taskId, date, getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/daily/{date}/close")
    public ResponseEntity<?> closeDay(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        dayCloseService.closeDayExplicit(getUserId(), date);
        return ResponseEntity.ok().build();
    }

    private String getUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Data
    public static class WeeklyPlanRequest {
        private int weekNumber;
        private int year;
        private Map<DayOfWeek, List<String>> taskGrid;
    }
}
