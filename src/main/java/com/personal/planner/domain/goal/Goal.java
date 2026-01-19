package com.personal.planner.domain.goal;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Model class for Goal, stored in the MongoDB database.
 * Collection name: goal
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "goal")
public class Goal {
    @Id
    private String id;
}
