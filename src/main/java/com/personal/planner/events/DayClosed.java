package com.personal.planner.events;

import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Factual data representing the closing of a daily cycle.
 * Implements {@link DomainEvent}.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class DayClosed implements DomainEvent {
    private LocalDate day;
    private String userId;
    private Instant closedAt;

    @Override
    public Instant occurredAt() {
        return closedAt;
    }

    @Override
    public String userId() {
        return userId;
    }
}
