package com.personal.planner.domain.common;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Production implementation of {@link ClockProvider} using system time.
 */
@Component
public class SystemClockProvider implements ClockProvider {

    @Override
    public LocalDate today() {
        return LocalDate.now(ZoneId.of("UTC"));
    }

    @Override
    public LocalDate today(ZoneId zoneId) {
        return LocalDate.now(zoneId);
    }

    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public ZonedDateTime zonedDateTime(ZoneId zoneId) {
        return ZonedDateTime.now(zoneId);
    }
}
