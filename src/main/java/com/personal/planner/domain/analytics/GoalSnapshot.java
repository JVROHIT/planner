package com.personal.planner.domain.analytics;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/**
 * GoalSnapshot represents a <b>historical fact</b> of progress at a specific
 * moment in time.
 * <p>
 * A GoalSnapshot is an append-only record that captures the progress state of a
 * Goal at a particular instant. These snapshots are immutable once created and
 * serve as the foundation for trend analysis, trajectory calculation, and historical
 * reporting. They enable the system to answer questions like "How did progress
 * change over time?" without modifying historical data.
 * </p>
 * <p>
 * Domain Invariants:
 * <ul>
 *   <li>Create once. Never modify after creation.</li>
 *   <li>Instances are immutable historical facts.</li>
 *   <li>Used for trend and trajectory analysis only.</li>
 *   <li>Must never be deleted or updated.</li>
 * </ul>
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "goalSnapshot")
public class GoalSnapshot {
    /** Unique identifier for this snapshot. */
    @Id
    private String id;

    /** Identifier of the Goal this snapshot represents. */
    private String goalId;

    /**
     * Progress value at the time of snapshot creation.
     * This value is frozen at creation time and never changes.
     */
    private double progress;

    /**
     * Timestamp when this snapshot was created.
     * Used for ordering snapshots chronologically for trend analysis.
     */
    private Instant snapshottedAt;
}
