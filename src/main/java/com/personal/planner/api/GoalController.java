package com.personal.planner.api;

import com.personal.planner.domain.analytics.GoalQueryService;
import com.personal.planner.domain.goal.Goal;
import com.personal.planner.domain.goal.GoalService;
import com.personal.planner.domain.goal.KeyResult;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing goals and key results.
 * All operations are scoped to the authenticated user.
 * 
 * <p>Ownership validation is performed by the domain service layer.
 * This controller only orchestrates request → service → response.</p>
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
     * @return list of active goals
     */
    @GetMapping
    public ResponseEntity<?> getGoals(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(goalQueryService.getActiveGoals(userId));
    }

    /**
     * Creates a new goal for the authenticated user.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param request the goal creation request
     * @return the created goal
     */
    @PostMapping
    public ResponseEntity<?> createGoal(@AuthenticationPrincipal String userId, @RequestBody GoalRequest request) {
        Goal goal = Goal.builder()
                .title(request.title)
                .description(request.description)
                .userId(userId)
                .build();
        return ResponseEntity.ok(goalService.createGoal(goal, userId));
    }

    /**
     * Updates an existing goal.
     * Ownership validation is performed by the service layer.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param id the goal ID to update
     * @param request the goal update request
     * @return the updated goal
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(@AuthenticationPrincipal String userId,
                                        @PathVariable String id,
                                        @RequestBody GoalRequest request) {
        Goal goal = Goal.builder()
                .id(id)
                .title(request.title)
                .description(request.description)
                .userId(userId)
                .build();
        return ResponseEntity.ok(goalService.updateGoal(goal, userId));
    }

    /**
     * Deletes a goal after verifying ownership.
     * Ownership validation is performed by the service layer.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param id the goal ID to delete
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@AuthenticationPrincipal String userId, @PathVariable String id) {
        goalService.deleteGoal(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Creates a new key result for a goal.
     * Ownership validation is performed by the service layer.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param goalId the goal ID this key result belongs to
     * @param request the key result creation request
     * @return the created key result
     */
    @PostMapping("/{goalId}/key-results")
    public ResponseEntity<?> createKeyResult(@AuthenticationPrincipal String userId,
                                              @PathVariable String goalId,
                                              @RequestBody KeyResultRequest request) {
        KeyResult kr = KeyResult.builder()
                .goalId(goalId)
                .title(request.title)
                .targetValue(request.targetValue)
                .type(request.type)
                .description(request.description)
                .build();
        return ResponseEntity.ok(goalService.createKeyResult(kr, userId));
    }

    /**
     * Updates an existing key result.
     * Ownership validation is performed by the service layer.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param id the key result ID to update
     * @param request the key result update request
     * @return the updated key result
     */
    @PutMapping("/key-results/{id}")
    public ResponseEntity<?> updateKeyResult(@AuthenticationPrincipal String userId,
                                            @PathVariable String id,
                                            @RequestBody KeyResultRequest request) {
        KeyResult kr = KeyResult.builder()
                .id(id)
                .title(request.title)
                .targetValue(request.targetValue)
                .type(request.type)
                .description(request.description)
                .build();
        return ResponseEntity.ok(goalService.updateKeyResult(kr, userId));
    }

    /**
     * Deletes a key result after verifying ownership.
     * Ownership validation is performed by the service layer.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param id the key result ID to delete
     * @return 204 No Content on success
     */
    @DeleteMapping("/key-results/{id}")
    public ResponseEntity<?> deleteKeyResult(@AuthenticationPrincipal String userId, @PathVariable String id) {
        goalService.deleteKeyResult(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marks a milestone key result as completed.
     * Ownership validation is performed by the service layer.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param id the key result ID to mark as complete
     * @return 200 OK on success
     */
    @PostMapping("/key-results/{id}/complete")
    public ResponseEntity<?> completeMilestone(@AuthenticationPrincipal String userId, @PathVariable String id) {
        goalService.completeMilestone(id, userId);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class GoalRequest {
        private String title;
        private String description;
    }

    @Data
    public static class KeyResultRequest {
        private String title;
        private double targetValue;
        private KeyResult.Type type;
        private String description;
    }
}
