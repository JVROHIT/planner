package com.personal.planner.domain.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * The only source of "current time" in the domain.
 * <p>
 * "Time is a first-class dependency."
 * "This prevents the system from lying about 'now' in tests or simulations."
 * </p>
 * <p>
 * Constraints:
 * - No domain or service class may call LocalDate.now(), Instant.now(), or
 * System.currentTimeMillis() directly.
 * </p>
 */
public interface ClockProvider {
    /**
     * Returns the current domain date in UTC.
     */
    LocalDate today();

    /**
     * Returns the current domain date for a specific timezone.
     */
    LocalDate today(ZoneId zoneId);

    /**
     * Returns the current domain instant.
     */
    Instant now();

    /**
     * Returns the current domain ZonedDateTime for a specific timezone.
     */
    ZonedDateTime zonedDateTime(ZoneId zoneId);
}
