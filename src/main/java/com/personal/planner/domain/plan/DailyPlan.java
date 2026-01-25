package com.personal.planner.domain.plan;

import com.personal.planner.domain.common.exception.DomainViolationException;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DailyPlan represents the <b>execution truth</b> for a specific day.
 * <p>
 * A DailyPlan captures what actually happened during a specific day, serving as
 * the immutable historical record of task execution. Once a day is closed, the
 * plan becomes a permanent fact that cannot be altered, ensuring data integrity
 * for historical analysis and streak calculations.
 * </p>
 * <p>
 * Domain Invariants:
 * <ul>
 *   <li>A DailyPlan is mutable only while {@code closed == false}.</li>
 *   <li>Once {@code closed == true}, the DailyPlan becomes an <b>immutable
 *       historical fact</b>.</li>
 *   <li>Attempts to modify a closed plan will result in a DomainViolationException.</li>
 * </ul>
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "dailyPlan")
public class DailyPlan {
    /** Unique identifier for this daily plan. */
    @Id
    private String id;

    /** Identifier of the user who owns this plan. */
    private String userId;

    /** The specific date this plan represents. */
    private LocalDate day;

    /**
     * Whether this plan has been closed (finalized).
     * When true, the plan becomes immutable and cannot be modified.
     */
    private boolean closed;

    /**
     * List of task executions for this day.
     * Each entry represents the execution status of a specific task.
     */
    @Builder.Default
    private List<TaskExecution> tasks = new ArrayList<>();

    /**
     * Represents the execution status of a specific task on this day.
     * Hardened to prevent external mutation.
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    public static class TaskExecution {
        /** Identifier of the task being executed. */
        private String taskId;

        /** Whether this task was completed on this day. */
        private boolean completed;

        protected void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }

    public List<TaskExecution> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Finalizes the execution truth for this day.
     */
    public void close() {
        this.closed = true;
    }

    public void markCompleted(String taskId) {
        ensureNotClosed();
        tasks.stream()
                .filter(t -> t.getTaskId().equals(taskId))
                .findFirst()
                .ifPresent(t -> t.setCompleted(true));
    }

    public void markMissed(String taskId) {
        ensureNotClosed();
        tasks.stream()
                .filter(t -> t.getTaskId().equals(taskId))
                .findFirst()
                .ifPresent(t -> t.setCompleted(false));
    }

    private void ensureNotClosed() {
        if (closed) {
            throw new DomainViolationException("Historical truth cannot be rewritten. Plan is closed for date: " + day);
        }
    }
}
