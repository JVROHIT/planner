package com.personal.planner.domain.goal;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Model class for KeyResult, stored in the MongoDB database.
 * Collection name: keyResult
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "keyResult")
public class KeyResult {
    @Id
    private String id;
}
