package com.personal.planner.domain.common;

import com.personal.planner.domain.common.constants.EventConstants;
import com.personal.planner.events.DayClosed;
import com.personal.planner.events.DomainEvent;
import com.personal.planner.events.TaskCompleted;
import com.personal.planner.events.TaskCreated;
import com.personal.planner.events.WeeklyPlanUpdated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Transforms DomainEvents into immutable, append-only audit log entries.
 * <p>
 * <strong>Purpose:</strong>
 * This service creates a factual, historical record of all domain activity.
 * It does not compute success metrics, streaks, or progress - it simply
 * captures what happened, when it happened, and to whom it happened.
 * </p>
 * <p>
 * <strong>Immutable Audit Log:</strong>
 * Audit events are immutable historical records that must never be modified
 * or deleted. Once recorded, they serve as the authoritative source of truth
 * for what occurred in the system. This append-only design ensures:
 * <ul>
 *   <li>Data integrity: Historical facts cannot be altered retroactively</li>
 *   <li>Audit trail: Complete record of all user actions and system events</li>
 *   <li>Compliance: Meets requirements for audit logging and accountability</li>
 *   <li>Replay capability: Events can be replayed for analytics or recovery</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Idempotency:</strong>
 * This service uses event receipts to ensure idempotent processing. If an event
 * has already been processed by this consumer, it will be skipped to prevent
 * duplicate audit entries. This is critical for:
 * <ul>
 *   <li>Preventing duplicate audit logs during event replay</li>
 *   <li>Handling retries and system restarts safely</li>
 *   <li>Maintaining data consistency across distributed systems</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Constraints:</strong>
 * <ul>
 *   <li>Does not compute success, streaks, or progress (meaningless capture)</li>
 *   <li>Simply mirrors facts into the archival log</li>
 *   <li>All audit events are immutable once created</li>
 *   <li>Unknown event types are logged as warnings but do not cause failures</li>
 * </ul>
 * </p>
 */
@Service
public class AuditService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditService.class);

    /**
     * Map-based dispatch for event type mapping.
     * Maps domain event classes to their corresponding audit event types.
     */
    private static final Map<Class<? extends DomainEvent>, AuditEvent.Type> EVENT_TYPE_MAP = Map.of(
            TaskCompleted.class, AuditEvent.Type.TASK_COMPLETED,
            TaskCreated.class, AuditEvent.Type.TASK_CREATED,
            DayClosed.class, AuditEvent.Type.DAY_CLOSED,
            WeeklyPlanUpdated.class, AuditEvent.Type.WEEKLY_PLAN_UPDATED
    );

    /**
     * Map-based dispatch for payload extraction.
     * Each function extracts event-specific data into the audit payload.
     */
    private static final Map<Class<? extends DomainEvent>, Function<DomainEvent, Map<String, Object>>> PAYLOAD_EXTRACTORS = Map.of(
            TaskCompleted.class, event -> {
                Map<String, Object> payload = new HashMap<>();
                payload.put("eventId", event.eventId());
                payload.put("taskId", ((TaskCompleted) event).getTaskId());
                return payload;
            },
            TaskCreated.class, event -> {
                Map<String, Object> payload = new HashMap<>();
                payload.put("eventId", event.eventId());
                payload.put("taskId", ((TaskCreated) event).getTaskId());
                return payload;
            },
            DayClosed.class, event -> {
                Map<String, Object> payload = new HashMap<>();
                payload.put("eventId", event.eventId());
                payload.put("day", ((DayClosed) event).getDay());
                return payload;
            },
            WeeklyPlanUpdated.class, event -> {
                Map<String, Object> payload = new HashMap<>();
                payload.put("eventId", event.eventId());
                payload.put("planId", ((WeeklyPlanUpdated) event).getPlanId());
                return payload;
            }
    );

    private final AuditRepository auditRepository;
    private final EventReceiptRepository eventReceiptRepository;
    private final ClockProvider clockProvider;

    public AuditService(
            AuditRepository auditRepository,
            EventReceiptRepository eventReceiptRepository,
            ClockProvider clockProvider) {
        this.auditRepository = auditRepository;
        this.eventReceiptRepository = eventReceiptRepository;
        this.clockProvider = clockProvider;
    }

    /**
     * Factual recorder of all domain activity.
     * <p>
     * Processes domain events and creates immutable audit log entries.
     * Implements idempotency checks to prevent duplicate processing.
     * </p>
     *
     * @param event the domain event to record in the audit log
     */
    @EventListener
    public void record(DomainEvent event) {
        // Idempotency check: skip if already processed
        if (isAlreadyProcessed(event.eventId())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("[AuditService] Event {} already processed, skipping", event.eventId());
            }
            return;
        }

        // Map event to audit type using dispatch map
        AuditEvent.Type type = EVENT_TYPE_MAP.get(event.getClass());
        if (type == null) {
            LOG.warn("[AuditService] Unknown event type: {}. Event {} will not be audited.",
                    event.getClass().getSimpleName(), event.eventId());
            return;
        }

        // Extract payload using dispatch map
        Function<DomainEvent, Map<String, Object>> extractor = PAYLOAD_EXTRACTORS.get(event.getClass());
        Map<String, Object> payload = extractor != null
                ? extractor.apply(event)
                : createDefaultPayload(event);

        // Create immutable audit event
        AuditEvent auditEvent = AuditEvent.builder()
                .userId(event.userId())
                .type(type)
                .payload(payload)
                .occurredAt(event.occurredAt())
                .build();

        // Persist audit event
        auditRepository.save(auditEvent);

        // Record receipt for idempotency
        recordReceipt(event.eventId());
    }

    /**
     * Checks if an event has already been processed by this consumer.
     *
     * @param eventId the unique identifier of the event
     * @return true if the event has already been processed, false otherwise
     */
    private boolean isAlreadyProcessed(String eventId) {
        return eventReceiptRepository.findByEventIdAndConsumer(eventId, EventConstants.CONSUMER_AUDIT)
                .isPresent();
    }

    /**
     * Records a receipt indicating this event has been processed.
     *
     * @param eventId the unique identifier of the processed event
     */
    private void recordReceipt(String eventId) {
        EventReceipt receipt = EventReceipt.of(
                eventId,
                EventConstants.CONSUMER_AUDIT,
                clockProvider.nowInstant()
        );
        eventReceiptRepository.save(receipt);
    }

    /**
     * Creates a default payload with only the event ID.
     * Used as fallback if no specific extractor is found (should not happen in normal operation).
     *
     * @param event the domain event
     * @return a map containing only the event ID
     */
    private Map<String, Object> createDefaultPayload(DomainEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", event.eventId());
        return payload;
    }
}
