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
 * "Execution truth. Immutable after close."
 * </p>
 * <p>
 * Invariant:
 * - A DailyPlan is mutable only while {@code closed == false}.
 * - Once {@code closed == true}, the DailyPlan becomes an <b>immutable
 * historical fact</b>.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
     * Hardened to prevent external mutation.
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    public static class TaskExecution {
        private String taskId;
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
