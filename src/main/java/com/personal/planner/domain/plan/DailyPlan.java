package com.personal.planner.domain.plan;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Model class for DailyPlan, stored in the MongoDB database.
 * Collection name: dailyPlan
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
}
