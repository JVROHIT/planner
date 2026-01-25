package com.personal.planner.infra.push;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.nudge.Nudge;
import com.personal.planner.domain.nudge.NudgeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static com.personal.planner.domain.common.constants.TimeConstants.ZONE_OFFSET;

/**
 * Orchestrator for delivering pending nudges.
 */
@Service
public class PushDispatcher {

    private final NudgeRepository nudgeRepository;
    private final PushGateway pushGateway;
    private final ClockProvider clock;

    public PushDispatcher(NudgeRepository nudgeRepository, PushGateway pushGateway, ClockProvider clock) {
        this.nudgeRepository = nudgeRepository;
        this.pushGateway = pushGateway;
        this.clock = clock;
    }

    /**
     * Periodic sweep to deliver due nudges.
     */
    @Scheduled(fixedDelay = 60000) // Every minute
    public void dispatch() {
        nudgeRepository.findByStatusAndScheduledForBefore(Nudge.Status.PENDING, clock.now().toInstant(ZONE_OFFSET))
                .forEach(nudge -> {
                    pushGateway.send(nudge);
                    nudge.setStatus(Nudge.Status.SENT);
                    nudgeRepository.save(nudge);
                });
    }
}
