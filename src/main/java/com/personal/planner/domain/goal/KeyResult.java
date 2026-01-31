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
     * Starting value for this key result.
     */
    @Setter(AccessLevel.PROTECTED)
    private double startValue;

    /** Target value that must be reached to consider this key result complete. */
    @Setter(AccessLevel.PROTECTED)
    private double targetValue;

    /**
     * Current progress value toward the target.
     * This value is derived from task completions and should not be set manually.
     */
    @Setter(AccessLevel.PROTECTED)
    private double currentValue;

    /**
     * Weight of this key result within its goal.
     * Used for weighted progress calculations.
     */
    @Setter(AccessLevel.PROTECTED)
    @Builder.Default
    private double weight = 1.0;

    /**
     * Type of key result, determining how progress is evaluated.
     * ACCUMULATIVE: Progress accumulates over time.
     * HABIT: Tracks consistency of behavior.
     * MILESTONE: Binary completion status.
     */
    @Setter(AccessLevel.PROTECTED)
    private Type type;

    /**
     * Calculated progress percentage (currentValue / targetValue).
     * This is automatically computed and should not be set manually.
     */
    @Setter(AccessLevel.PROTECTED)
    private double progress;

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
        recalculateProgress();
    }

    /**
     * Internal set mechanism for Evaluators.
     */
    protected void updateProgress(double absoluteValue) {
        this.currentValue = absoluteValue;
        recalculateProgress();
    }

    private void recalculateProgress() {
        if (this.targetValue <= 0) {
            this.progress = 0;
            return;
        }
        this.progress = this.currentValue / this.targetValue;
    }
}
