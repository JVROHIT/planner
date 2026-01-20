package com.personal.planner.domain.common;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.Map;

/**
 * Append-only factual log of user activity.
 * <p>
 * "This is not analytics. It is history."
 * </p>
 * <p>
 * Constraints:
 * - Instances are immutable historical records.
 * - Must never be modified or deleted.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "auditEvent")
public class AuditEvent {
    @Id
    private String id;
    private String userId;
    private Type type;
    private Map<String, Object> payload;
    private Instant occurredAt;

    public enum Type {
        TASK_CREATED,
        TASK_COMPLETED,
        DAY_CLOSED,
        WEEKLY_PLAN_UPDATED
    }
}
