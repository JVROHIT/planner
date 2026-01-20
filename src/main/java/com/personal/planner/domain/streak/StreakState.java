package com.personal.planner.domain.streak;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * StreakState represents <b>behavioral continuity</b>.
 * <p>
 * Invariant:
 * - StreakState is <b>derived from history</b> (specifically {@code DayClosed}
 * events).
 * - It MUST NEVER be edited directly by any user or service component.
 * - It is a interpretation of the "Truth" layer.
 * </p>
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
    private String userId;
    private int currentStreak;
}
