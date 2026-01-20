package com.personal.planner.events;

import lombok.*;
import java.time.Instant;

/**
 * Event representing the creation of a new user.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class UserCreated implements DomainEvent {
    private String id;
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
