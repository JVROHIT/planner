package com.personal.planner.domain.common;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/**
 * Records that a consumer has already handled a fact.
 * <p>
 * This ensures idempotency and replay safety across the system.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "eventReceipt")
public class EventReceipt {
    @Id
    private String id;
    private String eventId;
    private String consumer;
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
