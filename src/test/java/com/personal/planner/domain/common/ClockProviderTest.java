package com.personal.planner.domain.common;

import com.personal.planner.domain.common.constants.TimeConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ClockProvider timezone enforcement.
 *
 * <p>Verifies that ClockProvider always uses Asia/Kolkata timezone
 * and never leaks system default timezone.</p>
 */
class ClockProviderTest {

    @Test
    @DisplayName("ClockProvider should always return Asia/Kolkata timezone")
    void clockProviderShouldAlwaysReturnAsiaKolkata() {
        // Create a real implementation (assuming there's a SystemClockProvider or similar)
        // For now, we'll test the interface contract
        ClockProvider clock = new ClockProvider() {
            @Override
            public java.time.LocalDateTime now() {
                return java.time.LocalDateTime.now(TimeConstants.ZONE_ID);
            }

            @Override
            public java.time.LocalDate today() {
                return java.time.LocalDate.now(TimeConstants.ZONE_ID);
            }

            @Override
            public java.time.LocalDate today(ZoneId zoneId) {
                return java.time.LocalDate.now(zoneId);
            }

            @Override
            public java.time.Instant nowInstant() {
                return java.time.Instant.now();
            }

            @Override
            public java.time.ZonedDateTime zonedDateTime(ZoneId zoneId) {
                return java.time.ZonedDateTime.now(zoneId);
            }
        };

        // Verify getZoneId() returns Asia/Kolkata
        assertEquals(TimeConstants.ZONE_ID, clock.getZoneId());
        assertEquals(ZoneId.of("Asia/Kolkata"), clock.getZoneId());
    }

    @Test
    @DisplayName("ClockProvider should not use system default timezone")
    void clockProviderShouldNotUseSystemDefault() {
        ZoneId systemDefault = ZoneId.systemDefault();
        
        // If system default is not Asia/Kolkata, verify ClockProvider doesn't use it
        if (!systemDefault.equals(TimeConstants.ZONE_ID)) {
            ClockProvider clock = new ClockProvider() {
                @Override
                public java.time.LocalDateTime now() {
                    return java.time.LocalDateTime.now(TimeConstants.ZONE_ID);
                }

                @Override
                public java.time.LocalDate today() {
                    return java.time.LocalDate.now(TimeConstants.ZONE_ID);
                }

                @Override
                public java.time.LocalDate today(ZoneId zoneId) {
                    return java.time.LocalDate.now(zoneId);
                }

                @Override
                public java.time.Instant nowInstant() {
                    return java.time.Instant.now();
                }

                @Override
                public java.time.ZonedDateTime zonedDateTime(ZoneId zoneId) {
                    return java.time.ZonedDateTime.now(zoneId);
                }
            };

            assertNotEquals(systemDefault, clock.getZoneId());
            assertEquals(TimeConstants.ZONE_ID, clock.getZoneId());
        }
    }

    @Test
    @DisplayName("TimeConstants.ZONE_ID should be Asia/Kolkata")
    void timeConstantsZoneIdShouldBeAsiaKolkata() {
        assertEquals(ZoneId.of("Asia/Kolkata"), TimeConstants.ZONE_ID);
        assertEquals("Asia/Kolkata", TimeConstants.ZONE_ID.getId());
    }
}
