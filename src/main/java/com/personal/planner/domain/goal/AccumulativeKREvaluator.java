package com.personal.planner.domain.goal;

import com.personal.planner.domain.task.Task;
import com.personal.planner.domain.task.TaskRepository;
import com.personal.planner.events.DomainEvent;
import com.personal.planner.events.TaskCompleted;

import org.apache.el.stream.Optional;
import org.springframework.stereotype.Component;

/**
 * Evaluator for Accumulative Key Results.
 */
@Component
public class AccumulativeKREvaluator {

    private final TaskRepository taskRepository;

    public AccumulativeKREvaluator(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public boolean supports(KeyResult kr) {
        return kr.getType() == KeyResult.Type.ACCUMULATIVE;
    }

    public void handle(KeyResult kr, DomainEvent event) {
        if (event instanceof TaskCompleted) {
            TaskCompleted taskCompleted = (TaskCompleted) event;
            Task task = taskRepository.findById(taskCompleted.getTaskId()).orElse(null);
            if (kr.getId().equals(taskCompleted.getKeyResultId())) {
                if (task != null) {
                    kr.applyProgress(task.getContribution());
                }
            }
        }
    }
}
