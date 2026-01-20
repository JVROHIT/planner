package com.personal.planner.infra.push;

import com.personal.planner.domain.nudge.Nudge;

/**
 * Interface for out-of-process push delivery.
 * <p>
 * Constraints:
 * - Pure adapter. No business logic.
 * </p>
 */
public interface PushGateway {
    /**
     * Sends a nudge to the user's device/client.
     */
    void send(Nudge nudge);
}
