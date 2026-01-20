package com.personal.planner.events;

/**
 * Interface for publishing domain events within the system.
 * <p>
 * "Publishes irreversible facts inside the system."
 * "Does not interpret events."
 * </p>
 */
public interface DomainEventPublisher {
    /**
     * Publishes a domain event to all registered listeners.
     */
    void publish(DomainEvent event);
}
