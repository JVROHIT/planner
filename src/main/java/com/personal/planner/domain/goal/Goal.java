package com.personal.planner.domain.goal;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

/**
 * Goal represents a <b>directional and evaluative</b> objective.
 * <p>
 * A Goal provides long-term direction and serves as a container for related KeyResults.
 * Goals represent high-level objectives that users want to achieve, with progress
 * being derived from the completion and evaluation of associated KeyResults rather
 * than being set directly.
 * </p>
 * <p>
 * Domain Invariants:
 * <ul>
 *   <li>Goals provide long-term direction.</li>
 *   <li>Progress within a Goal is derived from the completion of related KeyResults.</li>
 *   <li>Goals do not directly track progress; they aggregate progress from KeyResults.</li>
 * </ul>
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Document(collection = "goal")
public class Goal {
    /** Unique identifier for this goal. */
    @Id
    private String id;

    /** Identifier of the user who owns this goal. */
    private String userId;

    /** Short, descriptive title of the goal. */
    private String title;

    /** Time horizon for this goal (MONTH, QUARTER, YEAR). */
    private Horizon horizon;

    /** Start date for this goal's evaluation window. */
    private LocalDate startDate;

    /** End date for this goal's evaluation window. */
    private LocalDate endDate;

    /** Current status of this goal. */
    private Status status;

    public enum Horizon {
        MONTH,
        QUARTER,
        YEAR
    }

    public enum Status {
        ACTIVE,
        COMPLETED,
        ARCHIVED
    }
}
