package com.personal.planner.api;

import com.personal.planner.domain.streak.StreakQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for viewing behavioral consistency status.
 */
@RestController
@RequestMapping("/api/streak")
public class StreakController {

    private final StreakQueryService streakQueryService;

    public StreakController(StreakQueryService streakQueryService) {
        this.streakQueryService = streakQueryService;
    }

    @GetMapping
    public ResponseEntity<?> getStreak(@RequestParam String userId) {
        return ResponseEntity.ok(streakQueryService.getCurrent(userId));
    }
}
