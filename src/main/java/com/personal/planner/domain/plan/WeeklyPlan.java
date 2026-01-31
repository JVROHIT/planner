package com.personal.planner.domain.plan;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WeeklyPlan represents an <b>editable horizon</b> of intent.
 * <p>
 * A WeeklyPlan provides a flexible, editable grid structure for organizing tasks
 * across the days of a week. Unlike DailyPlan, which becomes immutable after closing,
 * WeeklyPlan remains fully editable throughout the week, allowing users to adjust
 * their planned tasks as their intentions evolve.
 * </p>
 * <p>
 * Domain Characteristics:
 * <ul>
 *   <li>Represents a planning horizon for a specific week (identified by week start date).</li>
 *   <li>Maintains a grid mapping each day of the week to a list of planned task IDs.</li>
 *   <li>Remains mutable throughout the week to accommodate changing user intentions.</li>
 * </ul>
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Document(collection = "weeklyPlan")
public class WeeklyPlan {
    /** Unique identifier for this weekly plan. */
    @Id
    private String id;

    /** Identifier of the user who owns this plan. */
    private String userId;

    /** Start date of the week this plan represents. */
    private LocalDate weekStart;

    /**
     * Grid mapping each day of the week to a list of task IDs planned for that day.
     * This structure allows flexible task assignment across the week.
     */
    @Builder.Default
    private Map<LocalDate, List<String>> taskGrid = new HashMap<>();

    /** Timestamp when this weekly plan was last updated. */
    private Instant updatedAt;

    public List<String> getTasksFor(LocalDate date) {
        return taskGrid.getOrDefault(date, new ArrayList<>());
    }
}
