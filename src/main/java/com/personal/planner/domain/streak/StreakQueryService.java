package com.personal.planner.domain.streak;

import com.personal.planner.domain.common.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Read model service for querying streak state for UI and dashboards.
 * <p>
 * <b>Derived Interpretation Concept:</b>
 * This service provides read-only access to StreakState, which is a <i>derived interpretation</i>
 * of behavioral consistency. The streak value is computed by StreakService from the sequence
 * of DayClosed events and stored in StreakState. This query service never modifies the streak
 * - it only reads the current computed interpretation.
 * </p>
 * <p>
 * The streak state represents:
 * <ul>
 *   <li>A computed view of consecutive successful days</li>
 *   <li>Derived from immutable DailyPlan facts (closed days)</li>
 *   <li>Updated automatically by event handlers, never by direct user action</li>
 * </ul>
 * </p>
 * <p>
 * <b>Constraints:</b>
 * <ul>
 *   <li>Must never modify domain state</li>
 *   <li>Must not infer meaning beyond what already exists</li>
 *   <li>Read from repositories only</li>
 *   <li>No business logic - pure query operations</li>
 * </ul>
 * </p>
 */
@Service
public class StreakQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(StreakQueryService.class);

    private final StreakRepository streakRepository;

    public StreakQueryService(StreakRepository streakRepository) {
        this.streakRepository = streakRepository;
    }

    /**
     * Retrieves the current behavioral continuity status (streak) for a user.
     * <p>
     * Returns the current StreakState which represents the derived interpretation
     * of the user's consecutive successful days. The streak value is computed
     * from the sequence of DayClosed events and reflects the most recent
     * interpretation of behavioral consistency.
     * </p>
     * <p>
     * <b>Note:</b> Returns null if no streak state exists for the user, which
     * may occur if the user was created before streak tracking was implemented
     * or if no DayClosed events have been processed yet.
     * </p>
     *
     * @param userId the unique identifier of the user
     * @return the current StreakState for the user, or null if not found
     * @throws IllegalArgumentException if userId is null or empty
     */
    public StreakState getCurrent(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[StreakQueryService] Retrieving current streak for userId={}", userId);
        }

        StreakState state = streakRepository.findByUserId(userId).orElse(null);

        if (LogUtil.isDebugEnabled()) {
            if (state != null) {
                LOG.debug("[StreakQueryService] Found streak state: userId={}, currentStreak={}",
                        userId, state.getCurrentStreak());
            } else {
                LOG.debug("[StreakQueryService] No streak state found for userId={}", userId);
            }
        }

        return state;
    }
}
