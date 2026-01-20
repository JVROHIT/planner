package com.personal.planner.domain.goal;

import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Persistence boundary for Goal.
 * <p>
 * Constraints:
 * - Must not encode business rules.
 * </p>
 */
@Repository
public interface GoalRepository {
    Goal save(Goal goal);

    Optional<Goal> findById(String id);

    List<Goal> findByUserId(String userId);

    void deleteById(String id);
}
