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
 * Users can only view their own streak data.
 * All responses are wrapped in {@link ApiResponse} for consistent error handling.</p>
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
     * @return the current streak state wrapped in ApiResponse
     */
    @GetMapping
    public ResponseEntity<ApiResponse<StreakResponse>> getStreak(@AuthenticationPrincipal String userId) {
        StreakState state = streakQueryService.getCurrent(userId);
        int currentStreak = state != null ? state.getCurrentStreak() : 0;
        return ResponseEntity.ok(ApiResponse.success(new StreakResponse(currentStreak)));
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
