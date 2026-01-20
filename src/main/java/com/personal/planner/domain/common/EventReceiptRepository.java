package com.personal.planner.domain.common;

import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Persistence boundary for EventReceipt.
 * <p>
 * Constraints:
 * - Must not encode business rules.
 * </p>
 */
@Repository
public interface EventReceiptRepository {
    EventReceipt save(EventReceipt receipt);

    Optional<EventReceipt> findByEventIdAndConsumer(String eventId, String consumer);
}
