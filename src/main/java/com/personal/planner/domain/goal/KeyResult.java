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
    @Id
    @Setter(AccessLevel.PROTECTED)
    private String id;

    @Setter(AccessLevel.PROTECTED)
    private String goalId;

    @Setter(AccessLevel.PROTECTED)
    private String title;

    @Setter(AccessLevel.PROTECTED)
    private double currentProgress;

    @Setter(AccessLevel.PROTECTED)
    private double targetValue;

    @Setter(AccessLevel.PROTECTED)
    private Type type;

    public enum Type {
        ACCUMULATIVE,
        HABIT,
        MILESTONE
    }

    /**
     * Internal update mechanism for Evaluators within the domain package.
     */
    protected void applyProgress(double delta) {
        this.currentProgress += delta;
    }

    /**
     * Internal set mechanism for Evaluators.
     */
    protected void updateProgress(double absoluteValue) {
        this.currentProgress = absoluteValue;
    }
}
