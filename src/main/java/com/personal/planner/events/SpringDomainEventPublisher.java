package com.personal.planner.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring-based implementation of DomainEventPublisher.
 *
 * <p><strong>Purpose:</strong></p>
 * <p>This class bridges the domain layer's event publishing needs with
 * Spring's ApplicationEventPublisher infrastructure. It provides a clean
 * abstraction that allows the domain layer to publish events without
 * directly depending on Spring framework classes.</p>
 *
 * <p><strong>How it works:</strong></p>
 * <ol>
 *   <li>Domain services inject DomainEventPublisher interface</li>
 *   <li>This implementation delegates to Spring's ApplicationEventPublisher</li>
 *   <li>Spring's event bus delivers events to all registered @EventListener methods</li>
 *   <li>Event processing happens synchronously within the same transaction</li>
 * </ol>
 *
 * <p><strong>Event delivery characteristics:</strong></p>
 * <ul>
 *   <li><strong>In-process only:</strong> Events are delivered within the same JVM</li>
 *   <li><strong>Synchronous:</strong> All listeners execute before publish() returns</li>
 *   <li><strong>Transactional:</strong> Event processing shares the same transaction</li>
 *   <li><strong>Ordered:</strong> Listeners execute in registration order (not guaranteed)</li>
 * </ul>
 *
 * <p><strong>Error handling:</strong></p>
 * <p>If any event listener throws an exception, it will propagate back to the
 * publisher and potentially roll back the entire transaction. Listeners should
 * handle their own errors gracefully or use @Async for fire-and-forget processing.</p>
 *
 * <p><strong>Performance considerations:</strong></p>
 * <p>Since event processing is synchronous, heavy operations in listeners can
 * slow down the publishing thread. Consider using @Async or message queues
 * for time-consuming event processing.</p>
 *
 * <p><strong>Testing:</strong></p>
 * <p>In tests, you can use @MockBean to replace this publisher or use
 * @TestEventListener to verify event publishing behavior.</p>
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    /**
     * Creates a new Spring-based domain event publisher.
     *
     * @param publisher Spring's ApplicationEventPublisher for event delivery
     */
    public SpringDomainEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Publishes a domain event to all registered listeners.
     *
     * <p>This method delegates to Spring's ApplicationEventPublisher,
     * which will synchronously deliver the event to all @EventListener
     * methods that can handle the event type.</p>
     *
     * @param event the domain event to publish, must not be null
     * @throws RuntimeException if any event listener throws an exception
     */
    @Override
    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
}
