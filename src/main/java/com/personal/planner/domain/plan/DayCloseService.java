package com.personal.planner.domain.plan;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service layer with a scheduler to throw events on day close.
 */
@Service
public class DayCloseService {

    /**
     * Scheduler to trigger day close logic and throw events.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void onDayClose() {
        // Method to trigger day close event
    }
}
