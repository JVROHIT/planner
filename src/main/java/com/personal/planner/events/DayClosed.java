package com.personal.planner.events;

import lombok.*;
import java.time.LocalDate;

/**
 * Event template for Day Closed.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayClosed {
    private LocalDate date;
}
