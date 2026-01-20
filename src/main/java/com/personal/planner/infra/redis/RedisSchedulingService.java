package com.personal.planner.infra.redis;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.preference.UserPreference;
import com.personal.planner.domain.preference.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSchedulingService {

    private final StringRedisTemplate redisTemplate;
    private final UserPreferenceRepository preferenceRepository;
    private final ClockProvider clock;

    private static final String KEY_PREFIX = "planning_trigger:";

    /**
     * Schedules the next planning event for the user.
     * Calculates the time difference between NOW and the user's preferred time.
     */
    public void scheduleNextPlanning(String userId) {
        preferenceRepository.findByUserId(userId).ifPresent(prefs -> {
            schedule(userId, prefs);
        });
    }

    public void schedule(String userId, UserPreference prefs) {
        ZonedDateTime now = clock.zonedDateTime(prefs.getTimeZone());

        // Find next occurrence of DayOfWeek at Time
        ZonedDateTime nextRun = now.with(prefs.getPlanningTime());
        // Move to the correct day of week
        // Note: java.time DayOfWeek is 1-7 (Mon-Sun)
        // We want the upcoming occurrence.

        // First, set the day
        int currentDayVal = now.getDayOfWeek().getValue();
        int targetDayVal = prefs.getStartOfWeek().minus(1).getValue(); // Logic: Planning is 1 day before start

        int daysUntil = targetDayVal - currentDayVal;
        if (daysUntil < 0) {
            daysUntil += 7;
        }

        nextRun = nextRun.plusDays(daysUntil);

        // If the calculated time is in the past (e.g. today is the day, but time
        // passed), add 7 days
        if (nextRun.isBefore(now) || nextRun.isEqual(now)) {
            nextRun = nextRun.plusDays(7);
        }

        long secondsUntil = Duration.between(now, nextRun).getSeconds();

        String key = KEY_PREFIX + userId;
        // removing existing key if any
        redisTemplate.delete(key);

        // Set new key with TTL
        log.info("Scheduling planning for user {} in {} seconds (at {})", userId, secondsUntil, nextRun);
        redisTemplate.opsForValue().set(key, "pending", secondsUntil, TimeUnit.SECONDS);
    }

    /**
     * Listener for new user creation to kickstart the scheduling process.
     */
    @org.springframework.context.event.EventListener
    public void on(com.personal.planner.events.UserCreated event) {
        // Schedule using default preferences as valid user preference might not exist
        // yet
        // or we can rely on findByUserId returning defaults if missing.
        scheduleNextPlanning(event.userId());
    }
}
