package com.personal.planner.domain.analytics;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Model class for GoalSnapshot, stored in the MongoDB database.
 * Collection name: goalSnapshot
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "goalSnapshot")
public class GoalSnapshot {
    @Id
    private String id;
}
