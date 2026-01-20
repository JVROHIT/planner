package com.personal.planner.domain.common;

import lombok.AllArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Deterministic test implementation of {@link ClockProvider}.
 * <p>
 * Allows "freezing" or "traveling" in time during architectural testing.
 * </p>
 */
@Setter
@AllArgsConstructor
public class FixedClockProvider implements ClockProvider {

    private Instant fixedInstant;
    private ZoneId zoneId;

    @Override
    public LocalDate today() {
        return LocalDate.ofInstant(fixedInstant, zoneId);
    }

    @Override
    public LocalDate today(ZoneId zoneId) {
        return LocalDate.ofInstant(fixedInstant, zoneId);
    }

    @Override
    public Instant now() {
        return fixedInstant;
    }

    @Override
    public java.time.ZonedDateTime zonedDateTime(ZoneId zoneId) {
        return java.time.ZonedDateTime.ofInstant(fixedInstant, zoneId);
    }
}
