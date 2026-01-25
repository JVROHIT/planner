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
    /** Unique identifier for this streak state. */
    @Id
    private String id;

    /** Identifier of the user whose streak is being tracked. */
    private String userId;

    /**
     * Current streak count (consecutive days of activity).
     * This value is derived from DayClosed events and should be updated by domain
     * event handlers, not directly by application code.
     */
    @Setter
    private int currentStreak;
}
