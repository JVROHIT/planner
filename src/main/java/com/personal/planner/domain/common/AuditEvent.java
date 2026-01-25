package com.personal.planner.domain.common;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.Map;

/**
 * Append-only factual log of user activity.
 * <p>
 * AuditEvent represents an immutable historical record of something that happened
 * in the system. Unlike analytics or aggregated data, AuditEvents are raw facts
 * that capture what occurred, when it occurred, and relevant context. They form
 * the foundation for event sourcing, audit trails, and historical reconstruction.
 * </p>
 * <p>
 * Domain Invariants:
 * <ul>
 *   <li>Instances are immutable historical records.</li>
 *   <li>Must never be modified or deleted after creation.</li>
 *   <li>This is not analytics - it is history.</li>
 *   <li>Events are append-only; new events are added, existing events never change.</li>
 * </ul>
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "auditEvent")
public class AuditEvent {
    /** Unique identifier for this audit event. */
    @Id
    private String id;

    /** Identifier of the user who triggered this event. */
    private String userId;

    /**
     * Type of event that occurred.
     * Determines the structure and meaning of the payload data.
     */
    private Type type;

    /**
     * Event-specific data payload.
     * Structure varies by event type. Contains contextual information about what happened.
     */
    private Map<String, Object> payload;

    /**
     * Timestamp when this event occurred.
     * Used for chronological ordering and historical reconstruction.
     */
    private Instant occurredAt;

    public enum Type {
        TASK_CREATED,
        TASK_COMPLETED,
        DAY_CLOSED,
        WEEKLY_PLAN_UPDATED
    }
}
