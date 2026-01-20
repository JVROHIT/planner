package com.personal.planner.infra.redis;

import com.personal.planner.domain.plan.PlanningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Listens for Redis Key Expiration events to trigger planning.
 * Requires 'notify-keyspace-events Ex' in redis.conf.
 */
@Component
@Slf4j
public class PlanningExpirationListener extends KeyExpirationEventMessageListener {

    private final PlanningService planningService;
    private final RedisSchedulingService schedulingService;

    public PlanningExpirationListener(RedisMessageListenerContainer listenerContainer,
            PlanningService planningService,
            RedisSchedulingService schedulingService) {
        super(listenerContainer);
        this.planningService = planningService;
        this.schedulingService = schedulingService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        // Key format: "planning_trigger:{userId}"
        if (expiredKey.startsWith("planning_trigger:")) {
            String userId = expiredKey.split(":")[1];
            log.info("Planning trigger expired for user: {}", userId);

            try {
                // Generate the plan
                planningService.generateNextWeeklyPlan(userId);

                // Re-schedule for next week
                schedulingService.scheduleNextPlanning(userId);
            } catch (Exception e) {
                log.error("Failed to handle planning trigger for user " + userId, e);
            }
        }
    }
}
