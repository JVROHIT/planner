package com.personal.planner.domain.common;

import java.time.Instant;
import java.time.LocalDate;

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
     * Returns the current domain date.
     */
    LocalDate today();

    /**
     * Returns the current domain instant.
     */
    Instant now();
}
