package com.personal.planner.domain.plan;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

/**
 * WeeklyPlan represents an <b>editable horizon</b> of intent.
 * <p>
 * "Editable intent grid."
 * </p>
 * <p>
 * Invariant:
 * - A WeeklyPlan can be modified freely by the user.
 * - Changes to a WeeklyPlan MUST ONLY affect open or future days.
 * - It MUST NEVER alter the state of a DailyPlan that is already closed.
 * </p>
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
    private String userId;
    private int weekNumber;
    private int year;

    /**
     * Helper to verify if a specific date within this plan's horizon is still open
     * for modification.
     * <p>
     * Structural Rule: Closed days are untouchable.
     * </p>
     */
    public boolean isDayOpen(LocalDate date) {
        // Method stub: check structure vs truth layer
        return false;
    }
}
