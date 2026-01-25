package com.personal.planner.domain.common.constants;

import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Time-related constants for FocusFlow.
 * <p>
 * CRITICAL: All time operations MUST use ZONE_ID, never system default.
 * This ensures consistent behavior across different server environments and guarantees
 * that all users experience the application in the correct timezone (Asia/Kolkata).
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
     * The ONLY timezone used in FocusFlow - Asia/Kolkata (UTC+5:30).
     * <p>
     * MUST be used in all time-related operations:
     * - LocalDate.now(ZONE_ID)
     * - LocalDateTime.now(ZONE_ID)
     * - ZonedDateTime conversions
     * - Clock.system(ZONE_ID)
     * </p>
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
