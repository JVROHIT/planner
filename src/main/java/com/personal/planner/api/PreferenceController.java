package com.personal.planner.api;

import com.personal.planner.domain.preference.UserPreference;
import com.personal.planner.domain.preference.UserPreferenceRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;

import com.personal.planner.infra.redis.RedisSchedulingService;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final UserPreferenceRepository preferenceRepository;
    private final RedisSchedulingService schedulingService;

    @GetMapping
    public ResponseEntity<UserPreference> getPreferences(@AuthenticationPrincipal String userId) {
        return preferenceRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserPreference> createOrUpdatePreferences(
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

        return ResponseEntity.ok(saved);
    }

    @Data
    public static class PreferenceRequest {
        private String startOfWeek;
        private String planningTime;
        private String timeZone;
    }
}
