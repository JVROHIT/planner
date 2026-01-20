package com.personal.planner.domain.common;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Production implementation of {@link ClockProvider} using system time.
 */
@Component
public class SystemClockProvider implements ClockProvider {

    @Override
    public LocalDate today() {
        return LocalDate.now();
    }

    @Override
    public Instant now() {
        return Instant.now();
    }
}
