package com.personal.planner.events;

import lombok.*;
import java.time.Instant;

/**
 * Factual data representing the creation of a new task.
 * Implements {@link DomainEvent}.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class TaskCreated implements DomainEvent {
    private String id;
    private String taskId;
    private String userId;
    private Instant createdAt;

    @Override
    public Instant occurredAt() {
        return createdAt;
    }

    @Override
    public String eventId() {
        return id;
    }

    @Override
    public String userId() {
        return userId;
    }
}
