package com.personal.planner.domain.streak;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * StreakState represents the <b>current interpretation</b> of behavioral
 * consistency.
 * <p>
 * "Derived interpretation. Never edited directly."
 * </p>
 * <p>
 * Creation Rule:
 * - Create once. Never modify directly via UI.
 * - Meaning must emerge from the DayClosed event sequence.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "streakState")
public class StreakState {
    @Id
    private String id;
    private String userId;

    @Setter
    private int currentStreak;
}
