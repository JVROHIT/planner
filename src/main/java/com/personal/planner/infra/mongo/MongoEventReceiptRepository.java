package com.personal.planner.infra.mongo;

import com.personal.planner.domain.common.EventReceipt;
import com.personal.planner.domain.common.EventReceiptRepository;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the EventReceiptRepository.
 *
 * <p>Stores event receipts in a ConcurrentHashMap for thread-safe access.
 * Uses composite key of eventId:consumer for uniqueness.
 * This is a temporary implementation for development/testing - production
 * should use actual MongoDB collections.</p>
 *
 * <p>Event receipts are used for idempotency - ensuring each event
 * is only processed once per consumer (GOAL, STREAK, SNAPSHOT, AUDIT).</p>
 *
 * <p>Custom queries:
 * <ul>
 *   <li>findByEventIdAndConsumer: Checks if an event has been processed by a consumer</li>
 * </ul>
 * </p>
 */
@Component
public class MongoEventReceiptRepository implements EventReceiptRepository {

    private final Map<String, EventReceipt> store = new ConcurrentHashMap<>();

    @Override
    public EventReceipt save(EventReceipt receipt) {
        String key = receipt.getEventId() + ":" + receipt.getConsumer();
        store.put(key, receipt);
        return receipt;
    }

    @Override
    public Optional<EventReceipt> findByEventIdAndConsumer(String eventId, String consumer) {
        return Optional.ofNullable(store.get(eventId + ":" + consumer));
    }
}
