package com.personal.planner.infra.mongo;

import com.personal.planner.domain.common.AuditEvent;
import com.personal.planner.domain.common.AuditRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the AuditRepository.
 *
 * <p>Stores audit event entities in a ConcurrentHashMap for thread-safe access.
 * This is a temporary implementation for development/testing - production
 * should use actual MongoDB collections.</p>
 *
 * <p>Audit events are immutable records of what happened in the system.
 * They are append-only and should never be modified.</p>
 *
 * <p>Custom queries:
 * <ul>
 *   <li>findByUserIdOrderByOccurredAtDesc: Returns events for a user, most recent first</li>
 * </ul>
 * </p>
 */
@Component
public class MongoAuditRepository implements AuditRepository {

    private final Map<String, AuditEvent> store = new ConcurrentHashMap<>();

    @Override
    public AuditEvent save(AuditEvent event) {
        if (event.getId() == null) {
            // // Reflection-based ID injection
            try {
                java.lang.reflect.Field field = AuditEvent.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(event, java.util.UUID.randomUUID().toString());
            } catch (Exception e) {
                // ...
            }
        }
        store.put(event.getId(), event);
        return event;
    }

    @Override
    public List<AuditEvent> findByUserIdOrderByOccurredAtDesc(String userId) {
        return store.values().stream()
                .filter(e -> e.getUserId().equals(userId))
                .sorted((a, b) -> b.getOccurredAt().compareTo(a.getOccurredAt()))
                .collect(Collectors.toList());
    }
}
