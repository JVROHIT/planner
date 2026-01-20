package com.personal.planner.domain.nudge;

import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

/**
 * Persistence boundary for Nudge.
 */
@Repository
public interface NudgeRepository {
    Nudge save(Nudge nudge);

    List<Nudge> findByUserIdAndStatus(String userId, Nudge.Status status);

    List<Nudge> findByStatusAndScheduledForBefore(Nudge.Status status, Instant now);
}
