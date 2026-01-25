package com.personal.planner.domain.goal;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * KeyResult is an <b>evaluative unit</b> belonging to a Goal.
 * <p>
 * Invariant:
 * - Meaning (progress) is derived from facts, never injected manually.
 * - Modifications to progress must only happen via Evaluators.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "keyResult")
public class KeyResult {
    /** Unique identifier for this key result. */
    @Id
    @Setter(AccessLevel.PROTECTED)
    private String id;

    /** Identifier of the Goal this key result belongs to. */
    @Setter(AccessLevel.PROTECTED)
    private String goalId;

    /** Short, descriptive title of the key result. */
    @Setter(AccessLevel.PROTECTED)
    private String title;

    /**
     * Current progress value toward the target.
     * This value is derived from task completions and should not be set manually.
     */
    @Setter(AccessLevel.PROTECTED)
    private double currentValue;

    /** Target value that must be reached to consider this key result complete. */
    @Setter(AccessLevel.PROTECTED)
    private double targetValue;

    /**
     * Calculated progress percentage (currentValue / targetValue).
     * This is automatically computed and should not be set manually.
     */
    @Setter(AccessLevel.PROTECTED)
    private double progress;

    /**
     * Type of key result, determining how progress is evaluated.
     * ACCUMULATIVE: Progress accumulates over time.
     * HABIT: Tracks consistency of behavior.
     * MILESTONE: Binary completion status.
     */
    @Setter(AccessLevel.PROTECTED)
    private Type type;

    /** Detailed description explaining what this key result measures. */
    @Setter(AccessLevel.PROTECTED)
    private String description;

    public enum Type {  
        ACCUMULATIVE,
        HABIT,
        MILESTONE
    }

    /**
     * Internal update mechanism for Evaluators within the domain package.
     */
    protected void applyProgress(double delta) {
        this.currentValue += delta;
        this.progress = this.currentValue / this.targetValue;
    }

    /**
     * Internal set mechanism for Evaluators.
     */
    protected void updateProgress(double absoluteValue) {
        this.currentValue = absoluteValue;
    }
}
