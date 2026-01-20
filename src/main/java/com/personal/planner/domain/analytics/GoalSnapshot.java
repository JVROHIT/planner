package com.personal.planner.domain.analytics;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/**
 * GoalSnapshot represents a <b>historical fact</b> of progress at a specific
 * moment in time.
 * <p>
 * "Historical fact. Never mutated."
 * </p>
 * <p>
 * Creation Rule:
 * - Create once. Never modify.
 * - Used for trend and trajectory analysis only.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "goalSnapshot")
public class GoalSnapshot {
    @Id
    private String id;
    private String goalId;
    private double progress;
    private Instant snapshottedAt;
}
