package com.personal.planner.domain.preference;

import lombok.Builder;
import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * User specific configuration for the system.
 */
@Data
@Builder
public class UserPreference {
    private String userId;
    private DayOfWeek startOfWeek;
    private LocalTime planningTime;
    private ZoneId timeZone;

    public static UserPreference defaultPreferences(String userId) {
        return UserPreference.builder()
                .userId(userId)
                .startOfWeek(DayOfWeek.MONDAY)
                .planningTime(LocalTime.of(17, 0)) // 5 PM
                .timeZone(ZoneId.of("UTC"))
                .build();
    }
}
