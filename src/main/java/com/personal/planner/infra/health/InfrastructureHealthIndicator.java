package com.personal.planner.infra.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Health indicator for FocusFlow infrastructure components.
 *
 * <p>Checks connectivity to:
 * <ul>
 *   <li>MongoDB: Executes ping command</li>
 *   <li>Redis: Sets and gets a test key</li>
 * </ul>
 * </p>
 *
 * <p>Exposed via /actuator/health endpoint.</p>
 *
 * <p>IMPORTANT: This health check does NOT crash the application on failure.
 * It reports DOWN status with details for monitoring systems.</p>
 */
@Component
public class InfrastructureHealthIndicator implements HealthIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(InfrastructureHealthIndicator.class);

    /** Redis key used for health check read/write test. */
    private static final String REDIS_HEALTH_KEY = "health:check";

    /** Value written to Redis for health check. */
    private static final String REDIS_HEALTH_VALUE = "ok";

    private final MongoTemplate mongoTemplate;
    private final StringRedisTemplate redisTemplate;

    /**
     * Creates a new infrastructure health indicator.
     *
     * @param mongoTemplate MongoDB template for health check
     * @param redisTemplate Redis template for health check
     */
    public InfrastructureHealthIndicator(
            MongoTemplate mongoTemplate,
            StringRedisTemplate redisTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Performs health check on MongoDB and Redis.
     *
     * <p>For each component:
     * <ul>
     *   <li>If check succeeds, adds "Connected" status detail</li>
     *   <li>If check fails, sets overall status to DOWN with error message</li>
     * </ul>
     * </p>
     *
     * @return Health status with details for each component
     */
    @Override
    public Health health() {
        Health.Builder builder = Health.up();

        // Check MongoDB connectivity
        checkMongoDB(builder);

        // Check Redis connectivity
        checkRedis(builder);

        return builder.build();
    }

    /**
     * Checks MongoDB connectivity by executing ping command.
     *
     * @param builder the health builder to update
     */
    private void checkMongoDB(Health.Builder builder) {
        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            builder.withDetail("mongodb", "Connected");
        } catch (Exception e) {
            LOG.error("[HealthCheck] MongoDB check failed", e);
            builder.down().withDetail("mongodb", "Disconnected: " + e.getMessage());
        }
    }

    /**
     * Checks Redis connectivity by writing and reading a test key.
     *
     * @param builder the health builder to update
     */
    private void checkRedis(Health.Builder builder) {
        try {
            redisTemplate.opsForValue().set(REDIS_HEALTH_KEY, REDIS_HEALTH_VALUE);
            String value = redisTemplate.opsForValue().get(REDIS_HEALTH_KEY);
            if (REDIS_HEALTH_VALUE.equals(value)) {
                builder.withDetail("redis", "Connected");
            } else {
                builder.down().withDetail("redis", "Read/Write failed");
            }
        } catch (Exception e) {
            LOG.error("[HealthCheck] Redis check failed", e);
            builder.down().withDetail("redis", "Disconnected: " + e.getMessage());
        }
    }
}
