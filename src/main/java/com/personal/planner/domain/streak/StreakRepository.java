package com.personal.planner.domain.streak;

import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Persistence boundary for StreakState.
 * <p>
 * Constraints:
 * - Must not encode business rules.
 * </p>
 */
@Repository
public interface StreakRepository {
    StreakState save(StreakState streakState);

    Optional<StreakState> findByUserId(String userId);
}
