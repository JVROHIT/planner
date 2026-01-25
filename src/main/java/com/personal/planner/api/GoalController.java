package com.personal.planner.api;

import com.personal.planner.domain.analytics.GoalQueryService;
import com.personal.planner.domain.goal.Goal;
import com.personal.planner.domain.goal.GoalService;
import com.personal.planner.domain.goal.KeyResult;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing goals and key results. Identity-scoped via JWT.
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

    @GetMapping
    public ResponseEntity<?> getGoals() {
        return ResponseEntity.ok(goalQueryService.getActiveGoals(getUserId()));
    }

    @PostMapping
    public ResponseEntity<?> createGoal(@RequestBody GoalRequest request) {
        Goal goal = Goal.builder()
                .title(request.title)
                .description(request.description)
                .userId(getUserId())
                .build();
        return ResponseEntity.ok(goalService.createGoal(goal, getUserId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(@PathVariable String id, @RequestBody GoalRequest request) {
        // Validation: verify goal belongs to user before update
        Goal goal = Goal.builder()
                .id(id)
                .title(request.title)
                .description(request.description)
                .userId(getUserId())
                .build();
        return ResponseEntity.ok(goalService.updateGoal(goal, getUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable String id) {
        goalService.deleteGoal(id, getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{goalId}/key-results")
    public ResponseEntity<?> createKeyResult(@PathVariable String goalId, @RequestBody KeyResultRequest request) {
        KeyResult kr = KeyResult.builder()
                .goalId(goalId)
                .title(request.title)
                .targetValue(request.targetValue)
                .type(request.type)
                .description(request.description)
                .build();
        return ResponseEntity.ok(goalService.createKeyResult(kr, getUserId()));
    }

    @PutMapping("/key-results/{id}")
    public ResponseEntity<?> updateKeyResult(@PathVariable String id, @RequestBody KeyResultRequest request) {
        KeyResult kr = KeyResult.builder()
                .id(id)
                .title(request.title)
                .targetValue(request.targetValue)
                .type(request.type)
                .description(request.description)
                .build();
        return ResponseEntity.ok(goalService.updateKeyResult(kr, getUserId()));
    }

    @DeleteMapping("/key-results/{id}")
    public ResponseEntity<?> deleteKeyResult(@PathVariable String id) {
        goalService.deleteKeyResult(id, getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/key-results/{id}/complete")
    public ResponseEntity<?> completeMilestone(@PathVariable String id) {
        goalService.completeMilestone(id, getUserId());
        return ResponseEntity.ok().build();
    }

    private String getUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
