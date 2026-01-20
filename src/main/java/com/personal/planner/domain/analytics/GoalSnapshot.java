package com.personal.planner.domain.analytics;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/**
 * GoalSnapshot represents a <b>historical fact</b>.
 * <p>
 * "Represents a historical record of goal progress."
 * "Must never be updated after creation."
 * "Used for trend and trajectory analysis only."
 * </p>
 * <p>
 * Invariant:
 * - A GoalSnapshot captures the state of progress at a specific moment (usually
 * day close).
 * - Once created, it is <b>never mutated</b>.
 * - It provides the immutable data points needed for trend calculation.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "goalSnapshot")
public class GoalSnapshot {
    @Id
    private String id;
    private String goalId;
    private double progress;
    private Instant snapshottedAt;
}
