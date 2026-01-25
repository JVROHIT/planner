package com.personal.planner.domain.common;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/**
 * Records that a consumer has already handled a fact.
 * <p>
 * EventReceipt provides idempotency and replay safety in the event-driven architecture.
 * By recording which events have been processed by which consumers, the system can
 * safely replay events without duplicating side effects or processing the same event
 * multiple times.
 * </p>
 * <p>
 * Domain Characteristics:
 * <ul>
 *   <li>Each receipt represents a unique (eventId, consumer) pair.</li>
 *   <li>Used to prevent duplicate processing in event handlers.</li>
 *   <li>Enables safe event replay for recovery and debugging scenarios.</li>
 * </ul>
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "eventReceipt")
public class EventReceipt {
    /** Unique identifier for this receipt. */
    @Id
    private String id;

    /** Identifier of the event that was processed. */
    private String eventId;

    /**
     * Identifier of the consumer/handler that processed this event.
     * Used to track which handlers have processed which events.
     */
    private String consumer;

    /**
     * Timestamp when the event was processed by the consumer.
     * Used for auditing and debugging purposes.
     */
    private Instant processedAt;

    /**
     * Factory-style creation for an event receipt.
     */
    public static EventReceipt of(String eventId, String consumer, Instant processedAt) {
        return EventReceipt.builder()
                .eventId(eventId)
                .consumer(consumer)
                .processedAt(processedAt)
                .build();
    }
}
