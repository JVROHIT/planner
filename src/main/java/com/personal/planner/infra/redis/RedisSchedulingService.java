package com.personal.planner.infra.redis;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.preference.UserPreference;
import com.personal.planner.domain.preference.UserPreferenceRepository;
import com.personal.planner.domain.user.UserTimeZoneService;
import com.personal.planner.events.UserCreated;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.TimeUnit;

/**
 * Service for scheduling planning events using Redis key expiration.
 *
 * <p>Uses Redis TTL-based expiration to trigger planning events at the user's
 * preferred time. When a key expires, the PlanningExpirationListener picks up
 * the event and triggers the planning workflow.</p>
 *
 * <p>IMPORTANT: All time calculations use the user's timezone,
 * defaulting to Asia/Kolkata.</p>
 *
 * <p>Key format: planning_trigger:{userId}</p>
 */
@Service
@RequiredArgsConstructor
public class RedisSchedulingService {

    private static final Logger LOG = LoggerFactory.getLogger(RedisSchedulingService.class);

    /** Key prefix for planning trigger keys in Redis. */
    private static final String KEY_PREFIX = "planning_trigger:";

    /** Value stored in Redis for pending planning triggers. */
    private static final String PENDING_VALUE = "pending";

    private final StringRedisTemplate redisTemplate;
    private final UserPreferenceRepository preferenceRepository;
    private final UserTimeZoneService timeZoneService;
    private final ClockProvider clock;

    /**
     * Schedules the next planning event for a user.
     * Uses the user's preferences to determine when to trigger planning.
     *
     * @param userId the user ID to schedule planning for
     */
    public void scheduleNextPlanning(String userId) {
        preferenceRepository.findByUserId(userId).ifPresent(prefs -> {
            schedule(userId, prefs);
        });
    }

    /**
     * Schedules a planning event based on user preferences.
     * Calculates the next occurrence of the user's planning day/time
     * and sets a Redis key with TTL.
     *
     * <p>Planning is scheduled for one day before the user's start of week,
     * at the user's preferred planning time.</p>
     *
     * @param userId the user ID to schedule for
     * @param prefs the user's preferences
     */
    public void schedule(String userId, UserPreference prefs) {
        ZonedDateTime now = ZonedDateTime.now(timeZoneService.resolveZone(userId));

        // Calculate next planning time
        ZonedDateTime nextRun = calculateNextPlanningTime(now, prefs);

        // Calculate seconds until next run
        long secondsUntil = Duration.between(now, nextRun).getSeconds();

        // Set Redis key with TTL
        String key = buildKey(userId);
        setScheduleKey(key, secondsUntil);

        LOG.info("[RedisSchedulingService] Scheduled planning for user {} in {} seconds (at {})",
                userId, secondsUntil, nextRun);
    }

    /**
     * Calculates the next planning time based on user preferences.
     * Planning occurs one day before the user's start of week.
     *
     * @param now the current time
     * @param prefs the user's preferences
     * @return the next planning time
     */
    private ZonedDateTime calculateNextPlanningTime(ZonedDateTime now, UserPreference prefs) {
        LocalTime planningTime = prefs.getPlanningTime();
        DayOfWeek startOfWeek = prefs.getStartOfWeek();

        // Planning is one day before start of week
        DayOfWeek planningDay = startOfWeek.minus(1);

        // Start with today at the planning time
        ZonedDateTime nextRun = now.with(planningTime);

        // Adjust to the next occurrence of the planning day
        if (now.getDayOfWeek() == planningDay) {
            // Same day - check if time has passed
            if (nextRun.isBefore(now) || nextRun.isEqual(now)) {
                // Time passed, schedule for next week
                nextRun = nextRun.with(TemporalAdjusters.next(planningDay));
            }
        } else {
            // Different day - find next occurrence
            nextRun = nextRun.with(TemporalAdjusters.nextOrSame(planningDay));
            // If this results in today but time passed, go to next week
            if (nextRun.isBefore(now) || nextRun.isEqual(now)) {
                nextRun = nextRun.with(TemporalAdjusters.next(planningDay));
            }
        }

        return nextRun;
    }

    /**
     * Builds the Redis key for a user's planning trigger.
     *
     * @param userId the user ID
     * @return the Redis key
     */
    private String buildKey(String userId) {
        return KEY_PREFIX + userId;
    }

    /**
     * Sets the scheduling key in Redis with TTL.
     * Removes any existing key first.
     *
     * @param key the Redis key
     * @param secondsUntil seconds until expiration
     */
    private void setScheduleKey(String key, long secondsUntil) {
        // Remove existing key if any
        redisTemplate.delete(key);

        // Set new key with TTL
        redisTemplate.opsForValue().set(key, PENDING_VALUE, secondsUntil, TimeUnit.SECONDS);
    }

    /**
     * Listener for new user creation to kickstart the scheduling process.
     * Schedules the first planning event for newly created users.
     *
     * @param event the user created event
     */
    @EventListener
    public void on(UserCreated event) {
        LOG.debug("[RedisSchedulingService] User created, scheduling planning: {}", event.userId());
        scheduleNextPlanning(event.userId());
    }
}
