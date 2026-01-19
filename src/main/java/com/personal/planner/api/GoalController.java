package com.personal.planner.api;

import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing goals.
 * Supports: create, update, delete, and search goal objects.
 */
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    @PostMapping
    public void createGoal() {
        // Method to create a goal object
    }

    @PutMapping("/{id}")
    public void updateGoal(@PathVariable String id) {
        // Method to update a goal object
    }

    @DeleteMapping("/{id}")
    public void deleteGoal(@PathVariable String id) {
        // Method to delete a goal object
    }

    @GetMapping("/search")
    public void searchGoals() {
        // Method to search for goals
    }
}
