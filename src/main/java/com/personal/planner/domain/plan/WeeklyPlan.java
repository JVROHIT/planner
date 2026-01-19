package com.personal.planner.domain.plan;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Model class for WeeklyPlan, stored in the MongoDB database.
 * Collection name: weeklyPlan
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
}
