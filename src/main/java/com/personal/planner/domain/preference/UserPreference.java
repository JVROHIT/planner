package com.personal.planner.domain.preference;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * UserPreference represents user-specific configuration and settings.
 * <p>
 * UserPreferences store customizable settings that affect how the planning system
 * behaves for a specific user. These include temporal preferences (time zone, start
 * of week) and planning behavior preferences (when planning notifications occur).
 * </p>
 * <p>
 * Domain Characteristics:
 * <ul>
 *   <li>Each user has one preference record.</li>
 *   <li>Preferences can be updated to reflect changing user needs.</li>
 *   <li>Default preferences are provided via defaultPreferences() factory method.</li>
 * </ul>
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Document(collection = "userPreference")
public class UserPreference {
    /** Unique identifier for this preference record. */
    @Id
    private String id;

    /** Identifier of the user these preferences belong to. */
    private String userId;

    /**
     * Day of the week that the user considers the start of their week.
     * Used for weekly planning and reporting calculations.
     */
    private DayOfWeek startOfWeek;

    /**
     * Preferred time of day for planning activities.
     * Used for scheduling planning reminders and notifications.
     */
    private LocalTime planningTime;

    /**
     * User's time zone preference.
     * Used for converting timestamps and scheduling activities in the user's local time.
     */
    private ZoneId timeZone;

    public static UserPreference defaultPreferences(String userId) {
        return UserPreference.builder()
                .userId(userId)
                .startOfWeek(DayOfWeek.MONDAY)
                .planningTime(LocalTime.of(17, 0)) // 5 PM
                .timeZone(ZoneId.of("Asia/Kolkata"))
                .build();
    }
}
