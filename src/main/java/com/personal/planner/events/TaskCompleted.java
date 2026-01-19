package com.personal.planner.events;

import lombok.*;

/**
 * Event template for Task Completed.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCompleted {
    private String taskId;
}
