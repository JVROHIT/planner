package com.personal.planner.domain.streak;

import org.springframework.stereotype.Service;

/**
 * Read model for UI and dashboards.
 * <p>
 * Constraints:
 * - Must never modify domain state.
 * - Must not infer meaning beyond what already exists.
 * - Read from repositories only.
 * </p>
 */
@Service
public class StreakQueryService {

    private final StreakRepository streakRepository;

    public StreakQueryService(StreakRepository streakRepository) {
        this.streakRepository = streakRepository;
    }

    /**
     * Retrieves the current behavioral continuity status for a user.
     */
    public StreakState getCurrent(String userId) {
        return streakRepository.findByUserId(userId).orElse(null);
    }
}
