package com.personal.planner.domain.analytics;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

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

    /** Snapshot date (user-local day). */
    private LocalDate date;

    /** Actual progress at snapshot time (0..1). */
    private double actual;

    /** Expected progress at snapshot time (0..1). */
    private double expected;
}
