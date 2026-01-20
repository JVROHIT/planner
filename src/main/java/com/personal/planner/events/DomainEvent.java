package com.personal.planner.events;

import java.time.Instant;

/**
 * Base contract for all events in the system.
 * Represents an irreversible fact that has occurred.
 */
public interface DomainEvent {
    /**
     * @return The exact moment the fact occurred.
     */
    Instant occurredAt();

    /**
     * @return The unique identifier of this event instance.
     */
    String eventId();

    /**
     * @return The unique identifier of the user associated with this fact.
     */
    String userId();
}
