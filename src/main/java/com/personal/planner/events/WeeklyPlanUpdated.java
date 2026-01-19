package com.personal.planner.events;

import lombok.*;

/**
 * Event template for Weekly Plan Updated.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyPlanUpdated {
    private String planId;
}
