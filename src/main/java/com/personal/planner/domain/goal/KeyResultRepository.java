package com.personal.planner.domain.goal;

import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Persistence boundary for KeyResult.
 * <p>
 * Constraints:
 * - Must not encode business rules.
 * </p>
 */
@Repository
public interface KeyResultRepository {
    KeyResult save(KeyResult keyResult);

    Optional<KeyResult> findById(String id);

    List<KeyResult> findByGoalId(String goalId);
}
