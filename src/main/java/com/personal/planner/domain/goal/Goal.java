package com.personal.planner.domain.goal;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Goal represents a <b>directional and evaluative</b> objective.
 * <p>
 * Invariant:
 * - Goals provide long-term direction.
 * - Progress within a Goal is derived from the completion of related
 * KeyResults.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "goal")
public class Goal {
    @Id
    private String id;
    private String userId;
    private String title;
    private String description;
}
