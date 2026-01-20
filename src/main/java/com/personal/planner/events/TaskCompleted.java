package com.personal.planner.events;

import lombok.*;
import java.time.Instant;

/**
 * Factual data representing the completion of a task.
 * Implements {@link DomainEvent}.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class TaskCompleted implements DomainEvent {
    private String taskId;
    private String userId;
    private Instant completedAt;

    @Override
    public Instant occurredAt() {
        return completedAt;
    }

    @Override
    public String userId() {
        return userId;
    }
}
