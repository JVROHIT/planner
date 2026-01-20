package com.personal.planner.domain.common;

import com.personal.planner.events.DayClosed;
import com.personal.planner.events.DomainEvent;
import com.personal.planner.events.TaskCompleted;
import com.personal.planner.events.TaskCreated;
import com.personal.planner.events.WeeklyPlanUpdated;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * Transforms DomainEvents into user-visible history.
 * <p>
 * Constraints:
 * - Does not compute success, streaks, or progress. (Meaningless capture).
 * - Simply mirrors facts into the archival log.
 * </p>
 */
@Service
public class AuditService {

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Factual recorder of all domain activity.
     */
    @EventListener
    public void record(DomainEvent event) {
        AuditEvent.Type type = mapType(event);
        if (type == null)
            return;

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", event.eventId());

        // Extract specific payload bits based on event type
        if (event instanceof TaskCompleted e) {
            payload.put("taskId", e.getTaskId());
        } else if (event instanceof TaskCreated e) {
            payload.put("taskId", e.getTaskId());
        } else if (event instanceof DayClosed e) {
            payload.put("day", e.getDay());
        } else if (event instanceof WeeklyPlanUpdated e) {
            payload.put("planId", e.getPlanId());
        }

        AuditEvent auditEvent = AuditEvent.builder()
                .userId(event.userId())
                .type(type)
                .payload(payload)
                .occurredAt(event.occurredAt())
                .build();

        auditRepository.save(auditEvent);
    }

    private AuditEvent.Type mapType(DomainEvent event) {
        if (event instanceof TaskCompleted)
            return AuditEvent.Type.TASK_COMPLETED;
        if (event instanceof TaskCreated)
            return AuditEvent.Type.TASK_CREATED;
        if (event instanceof DayClosed)
            return AuditEvent.Type.DAY_CLOSED;
        if (event instanceof WeeklyPlanUpdated)
            return AuditEvent.Type.WEEKLY_PLAN_UPDATED;
        return null;
    }
}
