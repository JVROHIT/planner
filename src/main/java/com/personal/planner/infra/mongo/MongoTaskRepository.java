package com.personal.planner.infra.mongo;

import com.personal.planner.domain.task.Task;
import com.personal.planner.domain.task.TaskRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/**
 * Mongo implementation of the TaskRepository.
 */
@Component
public class MongoTaskRepository implements TaskRepository {

    @Override
    public Task save(Task task) {
        // // Mongo mapping goes here
        return task;
    }

    @Override
    public Optional<Task> findById(String id) {
        // // Mongo mapping goes here
        return Optional.empty();
    }

    @Override
    public void deleteById(String id) {
        // // Mongo mapping goes here
    }

    @Override
    public List<Task> findByUserId(String userId) {
        // // Mongo mapping goes here
        return List.of();
    }
}
