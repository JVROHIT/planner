package com.personal.planner.events;

import lombok.*;
import java.time.Instant;

/**
 * Factual data representing an update to a weekly plan.
 * Implements {@link DomainEvent}.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class WeeklyPlanUpdated implements DomainEvent {
    private String planId;
    private String userId;
    private Instant updatedAt;

    @Override
    public Instant occurredAt() {
        return updatedAt;
    }

    @Override
    public String userId() {
        return userId;
    }
}
