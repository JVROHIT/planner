package com.personal.planner.domain.goal;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * KeyResult is an <b>evaluative unit</b> belonging to a Goal.
 * <p>
 * Invariant:
 * - KeyResults define the measurable success of a Goal.
 * - They are mapped to Task completions or other factual markers.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "keyResult")
public class KeyResult {
    @Id
    private String id;
    private String goalId;
    private String title;
    private double currentProgress;
    private double targetValue;
    private Type type;

    public enum Type {
        ACCUMULATIVE,
        HABIT,
        MILESTONE
    }
}
