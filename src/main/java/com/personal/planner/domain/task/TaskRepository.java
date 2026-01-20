package com.personal.planner.domain.task;

import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Persistence boundary for Task.
 * <p>
 * Constraints:
 * - Must not encode business rules.
 * </p>
 */
@Repository
public interface TaskRepository {
    Task save(Task task);

    Optional<Task> findById(String id);

    void deleteById(String id);

    List<Task> findByUserId(String userId);
}
