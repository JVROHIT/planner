package com.personal.planner.domain.plan;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DailyPlan represents the <b>execution truth</b> for a specific day.
 * <p>
 * "Execution truth. Immutable after close."
 * </p>
 * <p>
 * Invariant:
 * - A DailyPlan is mutable only while {@code closed == false}.
 * - Once {@code closed == true}, the DailyPlan becomes an <b>immutable
 * historical fact</b>.
 * - No further modifications to tasks, status, or timestamps are permitted
 * after closure.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "dailyPlan")
public class DailyPlan {
    @Id
    private String id;
    private String userId;
    private LocalDate day;
    private boolean closed;

    @Builder.Default
    private List<TaskExecution> tasks = new ArrayList<>();

    /**
     * Represents the execution status of a specific task on this day.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskExecution {
        private String taskId;
        private boolean completed;
    }

    /**
     * Finalizes the execution truth for this day.
     * <p>
     * Structural Rule: Once closed, this instance is immutable fact.
     * </p>
     */
    public void close() {
        this.closed = true;
    }

    /**
     * Checks if the structural day has been frozen into history.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Marks a task as completed for this day.
     * <p>
     * Structural Rule: Closed days are untouchable.
     * </p>
     */
    public void markCompleted(String taskId) {
        if (closed) {
            throw new IllegalStateException("Cannot modify a closed DailyPlan");
        }
        tasks.stream()
                .filter(t -> t.getTaskId().equals(taskId))
                .findFirst()
                .ifPresent(t -> t.setCompleted(true));
    }

    /**
     * Marks a task as missed for this day.
     * <p>
     * Structural Rule: Closed days are untouchable.
     * </p>
     */
    public void markMissed(String taskId) {
        if (closed) {
            throw new IllegalStateException("Cannot modify a closed DailyPlan");
        }
        tasks.stream()
                .filter(t -> t.getTaskId().equals(taskId))
                .findFirst()
                .ifPresent(t -> t.setCompleted(false));
    }
}
