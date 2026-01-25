package com.personal.planner.domain.plan;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.DayOfWeek;
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
 *   <li>Represents a planning horizon for a specific week (identified by week number and year).</li>
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

    /** Week number within the year (1-52/53). */
    private int weekNumber;

    /** The year this weekly plan represents. */
    private int year;

    /**
     * Grid mapping each day of the week to a list of task IDs planned for that day.
     * This structure allows flexible task assignment across the week.
     */
    @Builder.Default
    private Map<DayOfWeek, List<String>> taskGrid = new HashMap<>();

    public List<String> getTasksFor(DayOfWeek dayOfWeek) {
        return taskGrid.getOrDefault(dayOfWeek, new ArrayList<>());
    }
}
