package com.personal.planner.infra.mongo;

import com.personal.planner.domain.common.EventReceipt;
import com.personal.planner.domain.common.EventReceiptRepository;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the EventReceiptRepository for end-to-end reality
 * check.
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
