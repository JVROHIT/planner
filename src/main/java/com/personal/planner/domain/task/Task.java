package com.personal.planner.domain.task;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Model class for Task, stored in the MongoDB database.
 * Collection name: task
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "task")
public class Task {
    @Id
    private String id;
    private String description;
    private boolean completed;
}
