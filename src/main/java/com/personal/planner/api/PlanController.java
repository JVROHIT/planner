package com.personal.planner.api;

import com.personal.planner.domain.plan.DailyPlanQueryService;
import com.personal.planner.domain.plan.PlanningService;
import com.personal.planner.domain.task.TaskService;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * Controller for managing weekly and daily plans.
 * <p>
 * "Controllers do not contain business logic."
 * "They validate input, call domain services, and shape responses."
 * "They must never compute analytics, streaks, or goal progress."
 * </p>
 * <p>
 * Boundaries:
 * - Can read/write WeeklyPlan.
 * - Can read DailyPlan.
 * - Must never compute streaks, trends, or goal progress.
 * </p>
 */
@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanningService planningService;
    private final TaskService taskService;
    private final DailyPlanQueryService dailyPlanQueryService;

    public PlanController(PlanningService planningService,
            TaskService taskService,
            DailyPlanQueryService dailyPlanQueryService) {
        this.planningService = planningService;
        this.taskService = taskService;
        this.dailyPlanQueryService = dailyPlanQueryService;
    }

    @GetMapping("/daily/today")
    public ResponseEntity<?> getToday(@RequestParam String userId) {
        // // Call QueryService
        // // Map to DTO
        return ResponseEntity.ok(dailyPlanQueryService.getToday(userId));
    }

    @GetMapping("/weekly/{weekStart}")
    public ResponseEntity<?> getWeeklyPlan(@RequestParam String userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        // // Call QueryService
        // // Map to DTO
        return ResponseEntity.ok(dailyPlanQueryService.getWeek(userId, weekStart));
    }

    @GetMapping("/daily/{userId}/{date}")
    public ResponseEntity<?> getDailyPlan(@PathVariable String userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // delegate to service
        // map to response DTO
        return ResponseEntity.ok().build();
    }

    @PostMapping("/daily/{date}/tasks/{taskId}/complete")
    public ResponseEntity<?> completeTask(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String taskId,
            @RequestParam String userId) {
        taskService.completeTask(taskId, date, userId);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class WeeklyPlanRequest {
        private String userId;
        private int weekNumber;
        private int year;
    }

    @Data
    public static class DailyPlanResponse {
        private String id;
        private LocalDate day;
        private boolean closed;
    }
}
