package com.personal.planner.domain.common.constants;

import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Time-related constants for FocusFlow.
 * <p>
 * Default time constants for the system.
 * Use ZONE_ID when a user-specific timezone is not configured.
 * </p>
 * <p>
 * Using system default timezone can lead to:
 * - Inconsistent date boundaries (day start/end times)
 * - Incorrect streak calculations
 * - Wrong snapshot timing
 * - Data integrity issues in analytics
 * </p>
 */
public final class TimeConstants {

    private TimeConstants() {
        // Prevent instantiation
    }

    /**
     * Default timezone used when a user has not configured one - Asia/Kolkata (UTC+5:30).
     */
    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Kolkata");
    public static final ZoneOffset ZONE_OFFSET = ZoneOffset.of("+05:30");

    /**
     * Standard date format for API requests and responses.
     * Format: yyyy-MM-dd
     * Example: 2026-01-25
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Standard datetime format for API requests and responses.
     * Format: yyyy-MM-dd'T'HH:mm:ss
     * Example: 2026-01-25T14:30:00
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
}
