package com.personal.planner.domain.common;

import com.personal.planner.domain.common.constants.TimeConstants;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * The only source of "current time" in the domain.
 * <p>
 * "Time is a first-class dependency."
 * "This prevents the system from lying about 'now' in tests or simulations."
 * </p>
 * <p>
 * <strong>CRITICAL TIMEZONE REQUIREMENT:</strong>
 * ALL time operations in FocusFlow MUST use Asia/Kolkata timezone (UTC+5:30).
 * This is enforced through {@link TimeConstants#ZONE_ID}.
 * </p>
 * <p>
 * <strong>Why Asia/Kolkata is mandatory:</strong>
 * <ul>
 *   <li>Consistent date boundaries across all server environments</li>
 *   <li>Accurate daily streak calculations</li>
 *   <li>Correct snapshot generation timing</li>
 *   <li>Data integrity in analytics and reporting</li>
 *   <li>Predictable user experience regardless of server location</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Constraints:</strong>
 * <ul>
 *   <li>No domain or service class may call LocalDate.now(), LocalDateTime.now(),
 *       Instant.now(), or System.currentTimeMillis() directly</li>
 *   <li>NEVER use system default timezone - always use TimeConstants.ZONE_ID</li>
 *   <li>All implementations MUST return time in Asia/Kolkata timezone</li>
 * </ul>
 * </p>
 */
public interface ClockProvider {

    /**
     * Returns the current date and time in Asia/Kolkata timezone.
     * <p>
     * This is the primary method for getting current timestamp with date and time components.
     * ALWAYS returns time in Asia/Kolkata (UTC+5:30), never in system default timezone.
     * </p>
     *
     * @return current LocalDateTime in Asia/Kolkata timezone
     */
    LocalDateTime now();

    /**
     * Returns the current date in Asia/Kolkata timezone.
     * <p>
     * Use this for date-only operations like daily streaks, snapshots, and analytics.
     * ALWAYS returns date in Asia/Kolkata (UTC+5:30), ensuring consistent day boundaries.
     * </p>
     *
     * @return current LocalDate in Asia/Kolkata timezone
     */
    LocalDate today();

    /**
     * Returns the current domain date for a specific timezone.
     * <p>
     * NOTE: This method is kept for backward compatibility but should be avoided.
     * Prefer using {@link #today()} which enforces Asia/Kolkata timezone.
     * </p>
     *
     * @param zoneId the timezone to use
     * @return current LocalDate in the specified timezone
     */
    LocalDate today(ZoneId zoneId);

    /**
     * Returns the current domain instant (timezone-independent).
     * <p>
     * Use this for absolute point-in-time operations (e.g., event timestamps, audit logs).
     * Instant is timezone-independent but should be converted to Asia/Kolkata for display.
     * </p>
     *
     * @return current Instant
     */
    Instant nowInstant();

    /**
     * Returns the current domain ZonedDateTime for a specific timezone.
     * <p>
     * NOTE: This method is kept for backward compatibility but should be avoided.
     * Prefer using {@link #now()} which enforces Asia/Kolkata timezone.
     * </p>
     *
     * @param zoneId the timezone to use
     * @return current ZonedDateTime in the specified timezone
     */
    ZonedDateTime zonedDateTime(ZoneId zoneId);

    /**
     * Returns the timezone used by this ClockProvider.
     * <p>
     * MUST return {@link TimeConstants#ZONE_ID} (Asia/Kolkata).
     * This is a safety check to ensure all implementations use the correct timezone.
     * </p>
     *
     * @return ZoneId.of("Asia/Kolkata")
     */
    default ZoneId getZoneId() {
        return TimeConstants.ZONE_ID;
    }
}
