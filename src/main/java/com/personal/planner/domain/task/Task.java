package com.personal.planner.domain.task;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Task represents an <b>intent unit</b>.
 * <p>
 * A Task is the fundamental unit of user intent in the planning system. It captures
 * what the user intends to do, serving as a mutable record of desire until it is
 * finalized by action. Tasks can be associated with Goals and KeyResults to track
 * progress toward larger objectives.
 * </p>
 * <p>
 * Domain Invariants:
 * <ul>
 *   <li>A Task captures what the user intends to do.</li>
 *   <li>It does NOT store execution history (e.g., when it was completed); that is
 *       the domain of Events.</li>
 *   <li>It is a mutable record of desire until it is finalized by action.</li>
 * </ul>
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Document(collection = "task")
public class Task {
    /** Unique identifier for this task. */
    @Id
    private String id;

    /** Identifier of the user who owns this task. */
    private String userId;

    /** Human-readable description of what needs to be done. */
    private String description;

    /** Whether this task has been marked as completed. */
    private boolean completed;

    /** Optional identifier of the Goal this task contributes to. */
    private String goalId;

    /** Optional identifier of the KeyResult this task contributes to. */
    private String keyResultId;

    /** Contribution value this task provides toward its associated KeyResult. */
    private long contribution;
}
