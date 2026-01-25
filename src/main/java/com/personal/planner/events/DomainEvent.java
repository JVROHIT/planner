package com.personal.planner.events;

import java.time.Instant;

/**
 * Base contract for all domain events in FocusFlow.
 *
 * <p>Domain events represent <strong>immutable facts</strong> that have occurred in the system.
 * They are NOT interpretations, calculations, or derived data - they are pure historical facts.</p>
 *
 * <p><strong>Event Contract:</strong></p>
 * <ul>
 *   <li><strong>Immutable:</strong> Once created, events cannot be modified</li>
 *   <li><strong>Unique:</strong> Each event has a unique eventId</li>
 *   <li><strong>Timestamped:</strong> Each event records when it occurred</li>
 *   <li><strong>User-scoped:</strong> Each event is associated with a specific user</li>
 * </ul>
 *
 * <p><strong>Facts vs. Interpretations:</strong></p>
 * <ul>
 *   <li>✅ FACT: "Task ABC was completed at 14:30"</li>
 *   <li>❌ INTERPRETATION: "User is productive today"</li>
 *   <li>✅ FACT: "Day 2024-01-15 was closed at 23:45"</li>
 *   <li>❌ INTERPRETATION: "User's streak increased to 5 days"</li>
 * </ul>
 *
 * <p><strong>Downstream Processing:</strong></p>
 * <p>Event consumers should:</p>
 * <ul>
 *   <li>React to facts by updating their own state</li>
 *   <li>Perform idempotent processing (same event = same result)</li>
 *   <li>NOT assume causality between events</li>
 *   <li>NOT infer business rules from event timing</li>
 * </ul>
 *
 * <p><strong>Implementation Requirements:</strong></p>
 * <ul>
 *   <li>All fields must be immutable (final or @Getter only)</li>
 *   <li>Use Lombok @Builder for construction</li>
 *   <li>Use @NoArgsConstructor(access = PRIVATE) for serialization</li>
 *   <li>eventId should be UUID.randomUUID().toString()</li>
 *   <li>occurredAt should use Instant.now() or ClockProvider</li>
 * </ul>
 */
public interface DomainEvent {
    /**
     * Returns the exact moment this fact occurred in the system.
     *
     * <p>This timestamp represents when the business event happened,
     * not when it was processed or stored. It should be set at the
     * moment of event creation in the domain layer.</p>
     *
     * @return the precise instant when this fact occurred
     */
    Instant occurredAt();

    /**
     * Returns the unique identifier for this specific event instance.
     *
     * <p>This ID is used for:</p>
     * <ul>
     *   <li>Idempotency checking (preventing duplicate processing)</li>
     *   <li>Event tracing and debugging</li>
     *   <li>Audit trails and event sourcing</li>
     * </ul>
     *
     * <p>Should be a UUID string generated at event creation time.</p>
     *
     * @return unique event identifier, never null
     */
    String eventId();

    /**
     * Returns the ID of the user associated with this fact.
     *
     * <p>All events in FocusFlow are user-scoped. This ID represents
     * the user whose action or state change triggered this event.</p>
     *
     * <p>Used for:</p>
     * <ul>
     *   <li>Data isolation and security</li>
     *   <li>User-specific event processing</li>
     *   <li>Analytics and reporting</li>
     * </ul>
     *
     * @return the user ID associated with this event, never null
     */
    String userId();
}
