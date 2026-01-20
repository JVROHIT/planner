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
 * "Editable intent grid."
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "weeklyPlan")
public class WeeklyPlan {
    @Id
    private String id;
    private String userId;
    private int weekNumber;
    private int year;

    /**
     * Grid of DayOfWeek -> List of Task IDs.
     */
    @Builder.Default
    private Map<DayOfWeek, List<String>> taskGrid = new HashMap<>();

    public List<String> getTasksFor(DayOfWeek dayOfWeek) {
        return taskGrid.getOrDefault(dayOfWeek, new ArrayList<>());
    }
}
