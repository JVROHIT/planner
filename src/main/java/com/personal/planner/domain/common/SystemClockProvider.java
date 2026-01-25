package com.personal.planner.domain.common;

import com.personal.planner.domain.common.constants.TimeConstants;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Production implementation of {@link ClockProvider} using real system time
 * but ALWAYS in Asia/Kolkata timezone.
 * <p>
 * This implementation provides actual current time from the system clock,
 * but ensures all date/time values are in Asia/Kolkata timezone (UTC+5:30).
 * </p>
 * <p>
 * CRITICAL: This class never uses system default timezone. All methods
 * explicitly use {@link TimeConstants#ZONE_ID} to guarantee consistent
 * behavior across different server environments.
 * </p>
 */
@Component
public class SystemClockProvider implements ClockProvider {

    @Override
    public LocalDateTime now() {
        // Always use Asia/Kolkata, never system default
        return LocalDateTime.now(TimeConstants.ZONE_ID);
    }

    @Override
    public LocalDate today() {
        // Always use Asia/Kolkata, never system default
        return LocalDate.now(TimeConstants.ZONE_ID);
    }

    @Override
    public LocalDate today(ZoneId zoneId) {
        return LocalDate.now(zoneId);
    }

    @Override
    public Instant nowInstant() {
        return Instant.now();
    }

    @Override
    public ZonedDateTime zonedDateTime(ZoneId zoneId) {
        return ZonedDateTime.now(zoneId);
    }
}
