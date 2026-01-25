package com.personal.planner.api;

import com.personal.planner.domain.common.exception.WeeklyPlanNotFoundException;
import com.personal.planner.domain.plan.DailyPlan;
import com.personal.planner.domain.plan.DailyPlanQueryService;
import com.personal.planner.domain.plan.DayCloseService;
import com.personal.planner.domain.plan.PlanningService;
import com.personal.planner.domain.plan.WeeklyPlan;
import com.personal.planner.domain.task.TaskService;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing weekly and daily plans.
 * All operations are scoped to the authenticated user.
 * 
 * <p>IMPORTANT: All dates are interpreted in Asia/Kolkata timezone.
 * This controller only orchestrates request → service → response.
 * Domain exceptions are handled by GlobalExceptionHandler.</p>
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

    /**
     * Creates a new weekly plan for the authenticated user.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param request the weekly plan creation request
     * @return the created weekly plan wrapped in ApiResponse
     */
    @PostMapping("/weekly-plan")
    public ResponseEntity<ApiResponse<WeeklyPlan>> createWeeklyPlan(
            @AuthenticationPrincipal String userId,
            @RequestBody WeeklyPlanRequest request) {
        WeeklyPlan plan = WeeklyPlan.builder()
                .userId(userId)
                .weekNumber(request.weekNumber)
                .year(request.year)
                .taskGrid(request.taskGrid)
                .build();
        return ResponseEntity.ok(ApiResponse.success(planningService.createWeeklyPlan(plan)));
    }

    /**
     * Retrieves a weekly plan for a specific date.
     * Date is interpreted in Asia/Kolkata timezone.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param date the date to get the weekly plan for (interpreted in Asia/Kolkata)
     * @return the weekly plan wrapped in ApiResponse
     * @throws WeeklyPlanNotFoundException if no plan exists for the specified week
     */
    @GetMapping("/weekly-plan/{date}")
    public ResponseEntity<ApiResponse<WeeklyPlan>> getWeeklyPlan(
            @AuthenticationPrincipal String userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        int weekNumber = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = date.get(IsoFields.WEEK_BASED_YEAR);
        return planningService.getWeeklyPlan(userId, weekNumber, year)
                .map(plan -> ResponseEntity.ok(ApiResponse.success(plan)))
                .orElseThrow(() -> new WeeklyPlanNotFoundException(
                        String.format("week %d of year %d", weekNumber, year)));
    }

    /**
     * Retrieves today's daily plan for the authenticated user.
     * Date is interpreted in Asia/Kolkata timezone.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @return today's daily plan wrapped in ApiResponse
     */
    @GetMapping("/daily/today")
    public ResponseEntity<ApiResponse<DailyPlan>> getToday(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.success(dailyPlanQueryService.getToday(userId)));
    }

    /**
     * Retrieves a daily plan for a specific date.
     * Date is interpreted in Asia/Kolkata timezone.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param date the date to get the daily plan for (interpreted in Asia/Kolkata)
     * @return the daily plan for the specified date wrapped in ApiResponse
     */
    @GetMapping("/daily/{date}")
    public ResponseEntity<ApiResponse<DailyPlan>> getDay(
            @AuthenticationPrincipal String userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(dailyPlanQueryService.getDay(userId, date)));
    }

    /**
     * Marks a task as completed for a specific date.
     * Date is interpreted in Asia/Kolkata timezone.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param date the date of the task execution (interpreted in Asia/Kolkata)
     * @param taskId the task ID to mark as completed
     * @return success response wrapped in ApiResponse
     */
    @PostMapping("/daily/{date}/tasks/{taskId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeTask(
            @AuthenticationPrincipal String userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String taskId) {
        taskService.completeTask(taskId, date, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * Marks a task as missed for a specific date.
     * Date is interpreted in Asia/Kolkata timezone.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param date the date of the task execution (interpreted in Asia/Kolkata)
     * @param taskId the task ID to mark as missed
     * @return success response wrapped in ApiResponse
     */
    @PostMapping("/daily/{date}/tasks/{taskId}/miss")
    public ResponseEntity<ApiResponse<Void>> missTask(
            @AuthenticationPrincipal String userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String taskId) {
        taskService.missTask(taskId, date, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * Closes a day, making the daily plan immutable.
     * Date is interpreted in Asia/Kolkata timezone.
     * 
     * <p>Once closed, a daily plan cannot be modified. Attempting to modify
     * a closed plan will result in a DomainViolationException (409 Conflict).</p>
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param date the date to close (interpreted in Asia/Kolkata)
     * @return success response wrapped in ApiResponse
     */
    @PostMapping("/daily/{date}/close")
    public ResponseEntity<ApiResponse<Void>> closeDay(
            @AuthenticationPrincipal String userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        dayCloseService.closeDayExplicit(userId, date);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Data
    public static class WeeklyPlanRequest {
        private int weekNumber;
        private int year;
        private Map<DayOfWeek, List<String>> taskGrid;
    }
}
