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
     * List of task entries for this day.
     * Each entry represents the execution status of a specific task.
     */
    @Builder.Default
    private List<Entry> entries = new ArrayList<>();

    /**
     * Represents the execution status of a specific task on this day.
     * Hardened to prevent external mutation.
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    public static class Entry {
        /** Identifier of the task being executed. */
        private String taskId;

        /** Snapshot of the task title for historical context. */
        private String title;

        /** Execution status for this task on this day. */
        private Status status;

        protected void setStatus(Status status) {
            this.status = status;
        }
    }

    public enum Status {
        PENDING,
        COMPLETED,
        MISSED
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public int getTotal() {
        return entries.size();
    }

    public int getCompleted() {
        return (int) entries.stream()
                .filter(e -> e.getStatus() == Status.COMPLETED)
                .count();
    }

    public double getRatio() {
        int total = getTotal();
        if (total == 0) {
            return 0;
        }
        return (double) getCompleted() / total;
    }

    /**
     * Finalizes the execution truth for this day.
     */
    public void close() {
        this.closed = true;
    }

    public void markCompleted(String taskId) {
        ensureNotClosed();
        entries.stream()
                .filter(t -> t.getTaskId().equals(taskId))
                .findFirst()
                .ifPresent(t -> t.setStatus(Status.COMPLETED));
    }

    public void markMissed(String taskId) {
        ensureNotClosed();
        entries.stream()
                .filter(t -> t.getTaskId().equals(taskId))
                .findFirst()
                .ifPresent(t -> t.setStatus(Status.MISSED));
    }

    private void ensureNotClosed() {
        if (closed) {
            throw new DomainViolationException("Historical truth cannot be rewritten. Plan is closed for date: " + day);
        }
    }
}
