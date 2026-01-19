package com.personal.planner.domain.streak;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Model class for StreakState, stored in the MongoDB database.
 * Collection name: streakState
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "streakState")
public class StreakState {
    @Id
    private String id;
}
