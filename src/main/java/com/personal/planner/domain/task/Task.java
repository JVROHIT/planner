package com.personal.planner.domain.task;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Task represents an <b>intent unit</b>.
 * <p>
 * Invariant:
 * - A Task captures what the user intends to do.
 * - It does NOT store execution history (e.g., when it was completed); that is
 * the domain of Events.
 * - It is a mutable record of desire until it is finalized by action.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "task")
public class Task {
    @Id
    private String id;
    private String userId;
    private String description;
    private boolean completed;
    private String goalId;
    private String keyResultId;
    private long contribution;
}
