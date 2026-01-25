package com.personal.planner.domain.common;

import com.personal.planner.domain.common.constants.TimeConstants;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Test implementation of {@link ClockProvider} with fixed time for deterministic behavior.
 * <p>
 * This implementation allows "freezing" or "traveling" in time during architectural testing,
 * enabling deterministic and reproducible test scenarios.
 * </p>
 * <p>
 * The fixed time is represented as an {@link Instant} which can be converted to any timezone.
 * By default, all timezone-specific operations use Asia/Kolkata (UTC+5:30) via
 * {@link TimeConstants#ZONE_ID} to match production behavior.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * // Fix time to a specific instant
 * Instant fixed = Instant.parse("2026-01-25T10:30:00Z");
 * ClockProvider clock = new FixedClockProvider(fixed, TimeConstants.ZONE_ID);
 *
 * // All calls return the same fixed time
 * LocalDateTime now = clock.now(); // 2026-01-25T16:00:00 (in IST)
 * LocalDate today = clock.today(); // 2026-01-25 (in IST)
 * </pre>
 * </p>
 */
@Setter
@AllArgsConstructor
public class FixedClockProvider implements ClockProvider {

    /**
     * The fixed point in time that this provider will always return.
     * This is timezone-independent and will be converted to the appropriate
     * timezone when calling timezone-specific methods.
     */
    private Instant fixedInstant;

    /**
     * The timezone used for converting the fixed instant to local dates/times.
     * Should typically be {@link TimeConstants#ZONE_ID} to match production behavior.
     */
    private ZoneId zoneId;

    /**
     * Returns the current date and time at the fixed instant in the configured timezone.
     * <p>
     * This converts the fixed instant to LocalDateTime in the timezone specified
     * during construction (typically Asia/Kolkata).
     * </p>
     *
     * @return fixed LocalDateTime in the configured timezone
     */
    @Override
    public LocalDateTime now() {
        // Convert fixed instant to LocalDateTime in the configured timezone
        return LocalDateTime.ofInstant(fixedInstant, zoneId);
    }

    /**
     * Returns the current date at the fixed instant in the configured timezone.
     * <p>
     * This converts the fixed instant to LocalDate in the timezone specified
     * during construction (typically Asia/Kolkata).
     * </p>
     *
     * @return fixed LocalDate in the configured timezone
     */
    @Override
    public LocalDate today() {
        // Convert fixed instant to LocalDate in the configured timezone
        return LocalDate.ofInstant(fixedInstant, zoneId);
    }

    /**
     * Returns the current date at the fixed instant in the specified timezone.
     * <p>
     * This allows testing timezone-specific behavior by converting the same
     * fixed instant to different timezones.
     * </p>
     *
     * @param zoneId the timezone to use for conversion
     * @return fixed LocalDate in the specified timezone
     */
    @Override
    public LocalDate today(ZoneId zoneId) {
        // Convert fixed instant to LocalDate in the specified timezone
        return LocalDate.ofInstant(fixedInstant, zoneId);
    }

    /**
     * Returns the fixed instant (timezone-independent).
     * <p>
     * This returns the exact fixed point in time that was set during construction.
     * </p>
     *
     * @return the fixed Instant
     */
    @Override
    public Instant nowInstant() {
        // Return the fixed instant unchanged
        return fixedInstant;
    }

    /**
     * Returns the current ZonedDateTime at the fixed instant in the specified timezone.
     * <p>
     * This allows testing timezone-specific behavior including offset information.
     * </p>
     *
     * @param zoneId the timezone to use for conversion
     * @return fixed ZonedDateTime in the specified timezone
     */
    @Override
    public ZonedDateTime zonedDateTime(ZoneId zoneId) {
        // Convert fixed instant to ZonedDateTime in the specified timezone
        return ZonedDateTime.ofInstant(fixedInstant, zoneId);
    }
}
