package com.personal.planner.api;

import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing weekly and daily plans.
 * Supports: weekly plan creation/update, weekly plan search, daily plan search.
 */
@RestController
@RequestMapping("/api/plans")
public class PlanController {

    @PostMapping("/weekly")
    public void createOrUpdateWeeklyPlan() {
        // Method to create or update a weekly plan
    }

    @GetMapping("/weekly/search")
    public void searchWeeklyPlan() {
        // Method to search for weekly plans
    }

    @GetMapping("/daily/search")
    public void searchDailyPlan() {
        // Method to search for daily plans
    }
}
