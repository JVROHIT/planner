package com.personal.planner.infra.mongo;

import com.personal.planner.domain.task.Task;
import com.personal.planner.domain.task.TaskRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the TaskRepository.
 *
 * <p>Stores task entities in a ConcurrentHashMap for thread-safe access.
 * This is a temporary implementation for development/testing - production
 * should use actual MongoDB collections.</p>
 *
 * <p>Tasks represent user intent units - things the user wants to accomplish.
 * They can be linked to goals and key results for progress tracking.</p>
 *
 * <p>Custom queries:
 * <ul>
 *   <li>findByUserId: Returns all tasks for a specific user</li>
 * </ul>
 * </p>
 */
@Component
public class MongoTaskRepository implements TaskRepository {

    private final Map<String, Task> store = new ConcurrentHashMap<>();

    @Override
    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(java.util.UUID.randomUUID().toString());
        }
        store.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }

    @Override
    public List<Task> findByUserId(String userId) {
        return store.values().stream()
                .filter(t -> t.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
