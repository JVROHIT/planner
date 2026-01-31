package com.personal.planner.api;

import com.personal.planner.domain.analytics.GoalQueryService;
import com.personal.planner.domain.goal.Goal;
import com.personal.planner.domain.goal.GoalService;
import com.personal.planner.domain.goal.KeyResult;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing goals and key results.
 * All operations are scoped to the authenticated user.
 * 
 * <p>Ownership validation is performed by the domain service layer.
 * This controller only orchestrates request → service → response.
 * All responses are wrapped in {@link ApiResponse} for consistent error handling.</p>
 */
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;
    private final GoalQueryService goalQueryService;

    public GoalController(GoalService goalService, GoalQueryService goalQueryService) {
        this.goalService = goalService;
        this.goalQueryService = goalQueryService;
    }

    /**
     * Retrieves all active goals for the authenticated user.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @return list of active goals wrapped in ApiResponse
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Goal>>> getGoals(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.success(goalQueryService.getActiveGoals(userId)));
    }

    /**
     * Creates a new goal for the authenticated user.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param request the goal creation request
     * @return the created goal wrapped in ApiResponse
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Goal>> createGoal(
            @AuthenticationPrincipal String userId, 
            @RequestBody GoalRequest request) {
        Goal goal = Goal.builder()
                .title(request.title)
                .horizon(request.horizon)
                .startDate(request.startDate)
                .endDate(request.endDate)
                .status(request.status)
                .userId(userId)
                .build();
        return ResponseEntity.ok(ApiResponse.success(goalService.createGoal(goal, userId)));
    }

    /**
     * Updates an existing goal.
     * Ownership validation is performed by the service layer.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param id the goal ID to update
     * @param request the goal update request
     * @return the updated goal wrapped in ApiResponse
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Goal>> updateGoal(
            @AuthenticationPrincipal String userId,
            @PathVariable String id,
            @RequestBody GoalRequest request) {
        Goal goal = Goal.builder()
                .id(id)
                .title(request.title)
                .horizon(request.horizon)
                .startDate(request.startDate)
                .endDate(request.endDate)
                .status(request.status)
                .build();
        return ResponseEntity.ok(ApiResponse.success(goalService.updateGoal(goal, userId)));
    }

    /**
     * Deletes a goal after verifying ownership.
     * Ownership validation is performed by the service layer.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param id the goal ID to delete
     * @return success response wrapped in ApiResponse
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @AuthenticationPrincipal String userId, 
            @PathVariable String id) {
        goalService.deleteGoal(id, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * Creates a new key result for a goal.
     * Ownership validation is performed by the service layer.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param goalId the goal ID this key result belongs to
     * @param request the key result creation request
     * @return the created key result wrapped in ApiResponse
     */
    @PostMapping("/{goalId}/key-results")
    public ResponseEntity<ApiResponse<KeyResult>> createKeyResult(
            @AuthenticationPrincipal String userId,
            @PathVariable String goalId,
            @RequestBody KeyResultRequest request) {
        KeyResult kr = KeyResult.builder()
                .goalId(goalId)
                .title(request.title)
                .startValue(request.startValue)
                .currentValue(request.currentValue)
                .targetValue(request.targetValue)
                .type(request.type)
                .weight(request.weight)
                .build();
        return ResponseEntity.ok(ApiResponse.success(goalService.createKeyResult(kr, userId)));
    }

    /**
     * Updates an existing key result.
     * Ownership validation is performed by the service layer.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param id the key result ID to update
     * @param request the key result update request
     * @return the updated key result wrapped in ApiResponse
     */
    @PutMapping("/key-results/{id}")
    public ResponseEntity<ApiResponse<KeyResult>> updateKeyResult(
            @AuthenticationPrincipal String userId,
            @PathVariable String id,
            @RequestBody KeyResultRequest request) {
        KeyResult kr = KeyResult.builder()
                .id(id)
                .title(request.title)
                .startValue(request.startValue)
                .currentValue(request.currentValue)
                .targetValue(request.targetValue)
                .type(request.type)
                .weight(request.weight)
                .build();
        return ResponseEntity.ok(ApiResponse.success(goalService.updateKeyResult(kr, userId)));
    }

    /**
     * Deletes a key result after verifying ownership.
     * Ownership validation is performed by the service layer.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param id the key result ID to delete
     * @return success response wrapped in ApiResponse
     */
    @DeleteMapping("/key-results/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteKeyResult(
            @AuthenticationPrincipal String userId, 
            @PathVariable String id) {
        goalService.deleteKeyResult(id, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * Marks a milestone key result as completed.
     * Ownership validation is performed by the service layer.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param id the key result ID to mark as complete
     * @return success response wrapped in ApiResponse
     */
    @PostMapping("/key-results/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> completeMilestone(
            @AuthenticationPrincipal String userId, 
            @PathVariable String id) {
        goalService.completeMilestone(id, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Data
    public static class GoalRequest {
        private String title;
        private Goal.Horizon horizon;
        private java.time.LocalDate startDate;
        private java.time.LocalDate endDate;
        private Goal.Status status;
    }

    @Data
    public static class KeyResultRequest {
        private String title;
        private double startValue;
        private double currentValue;
        private double targetValue;
        private double weight;
        private KeyResult.Type type;
    }
}
