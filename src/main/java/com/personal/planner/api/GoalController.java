package com.personal.planner.api;

import com.personal.planner.domain.analytics.GoalQueryService;
import com.personal.planner.domain.goal.GoalService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing goals and key results.
 * <p>
 * "Controllers do not contain business logic."
 * "They validate input, call domain services, and shape responses."
 * "They must never compute analytics, streaks, or goal progress."
 * </p>
 * <p>
 * Boundaries:
 * - Can CRUD Goals and KeyResults.
 * - Must never read DailyPlan or compute history.
 * </p>
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

    @GetMapping("/active")
    public ResponseEntity<?> getActiveGoals(@RequestParam String userId) {
        // // Call QueryService
        // // Map to DTO
        return ResponseEntity.ok(goalQueryService.getActiveGoals(userId));
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<?> getGoalProgress(@PathVariable String id) {
        // // Call QueryService
        // // Map to DTO
        return ResponseEntity.ok(goalQueryService.getKeyResults(id));
    }

    @PostMapping
    public ResponseEntity<?> createGoal(@RequestBody GoalRequest request) {
        // validate input
        // delegate to service
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(@PathVariable String id, @RequestBody GoalRequest request) {
        // validate input
        // delegate to service
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable String id) {
        // delegate to service
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> listGoals(@PathVariable String userId) {
        // delegate to service
        // map to response DTO
        return ResponseEntity.ok().build();
    }

    @Data
    public static class GoalRequest {
        private String title;
        private String userId;
    }

    @Data
    public static class GoalResponse {
        private String id;
        private String title;
    }
}
