package com.personal.planner.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link DomainEventPublisher} using Spring's
 * {@link ApplicationEventPublisher}.
 * <p>
 * "Preserves temporal truth and decoupling by delegating to the application
 * context's bus."
 * </p>
 */
@Component
public class InProcessEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    public InProcessEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(DomainEvent event) {
        // No logic beyond forwarding facts to the infrastructure bus
        publisher.publishEvent(event);
    }
}
