package com.personal.planner.domain.nudge;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/**
 * A suggestion derived from meaning. Not a fact.
 * <p>
 * "Guidance layer. Ephemeral and non-authoritative."
 * </p>
 * <p>
 * Constraints:
 * - Must never be used as a source of truth for history.
 * - Can be deleted without corrupting the domain state.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "nudge")
public class Nudge {
    @Id
    private String id;
    private String userId;
    private String type;
    private String message;
    private Instant scheduledFor;
    private Instant createdAt;

    @Setter
    private Status status;

    public enum Status {
        PENDING, SENT, CANCELLED
    }
}
