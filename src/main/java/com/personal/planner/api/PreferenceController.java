package com.personal.planner.api;

import com.personal.planner.domain.common.exception.EntityNotFoundException;
import com.personal.planner.domain.preference.UserPreference;
import com.personal.planner.domain.preference.UserPreferenceRepository;
import com.personal.planner.infra.redis.RedisSchedulingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Controller for managing user preferences.
 * All operations are scoped to the authenticated user.
 * 
 * <p>All responses are wrapped in {@link ApiResponse} for consistent error handling.</p>
 */
@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final UserPreferenceRepository preferenceRepository;
    private final RedisSchedulingService schedulingService;

    /**
     * Retrieves preferences for the authenticated user.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @return user preferences wrapped in ApiResponse
     * @throws EntityNotFoundException if preferences not found for user
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserPreference>> getPreferences(@AuthenticationPrincipal String userId) {
        return preferenceRepository.findByUserId(userId)
                .map(pref -> ResponseEntity.ok(ApiResponse.success(pref)))
                .orElseThrow(() -> new EntityNotFoundException("UserPreference", userId));
    }

    /**
     * Creates or updates preferences for the authenticated user.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param request the preference update request
     * @return saved preferences wrapped in ApiResponse
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserPreference>> createOrUpdatePreferences(
            @AuthenticationPrincipal String userId,
            @RequestBody PreferenceRequest request) {

        UserPreference preference = UserPreference.builder()
                .userId(userId)
                .startOfWeek(DayOfWeek.valueOf(request.getStartOfWeek().toUpperCase()))
                .planningTime(LocalTime.parse(request.getPlanningTime()))
                .timeZone(ZoneId.of(request.getTimeZone()))
                .build();

        UserPreference saved = preferenceRepository.save(preference);
        schedulingService.schedule(userId, saved);

        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @Data
    public static class PreferenceRequest {
        private String startOfWeek;
        private String planningTime;
        private String timeZone;
    }
}
