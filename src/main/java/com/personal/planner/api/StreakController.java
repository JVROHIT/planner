package com.personal.planner.api;

import com.personal.planner.domain.streak.StreakQueryService;
import com.personal.planner.domain.streak.StreakState;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for viewing behavioral consistency status (streak).
 * All endpoints are user-scoped via authentication.
 * 
 * <p>SECURITY: userId comes from authentication, never from request parameters.
 * Users can only view their own streak data.</p>
 */
@RestController
@RequestMapping("/api/streak")
public class StreakController {

    private final StreakQueryService streakQueryService;

    public StreakController(StreakQueryService streakQueryService) {
        this.streakQueryService = streakQueryService;
    }

    /**
     * Gets the current streak for the authenticated user.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @return the current streak state
     */
    @GetMapping
    public ResponseEntity<StreakResponse> getStreak(@AuthenticationPrincipal String userId) {
        StreakState state = streakQueryService.getCurrent(userId);
        if (state == null) {
            return ResponseEntity.ok(new StreakResponse(0));
        }
        return ResponseEntity.ok(new StreakResponse(state.getCurrentStreak()));
    }

    /**
     * Response DTO for streak endpoint.
     */
    @Data
    public static class StreakResponse {
        private final int currentStreak;

        public StreakResponse(int currentStreak) {
            this.currentStreak = currentStreak;
        }
    }
}
