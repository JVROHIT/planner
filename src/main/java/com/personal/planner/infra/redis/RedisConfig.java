package com.personal.planner.infra.redis;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Redis configuration for FocusFlow.
 *
 * <p>All connection settings are driven by environment variables:
 * <ul>
 *   <li>REDIS_HOST: Redis server hostname (required)</li>
 *   <li>REDIS_PORT: Redis server port (1-65535)</li>
 *   <li>REDIS_PASSWORD: Redis password (optional)</li>
 *   <li>REDIS_DATABASE: Redis database index (0-15)</li>
 *   <li>REDIS_TIMEOUT: Connection timeout in milliseconds</li>
 * </ul>
 * </p>
 *
 * <p>This configuration validates required settings on startup
 * and fails fast if Redis is not properly configured.</p>
 *
 * <p>IMPORTANT: Redis must have keyspace notifications enabled for
 * expiration events. Configure Redis with: notify-keyspace-events Ex</p>
 */
@Configuration
public class RedisConfig {

    private static final Logger LOG = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.data.redis.timeout:2000}")
    private long redisTimeout;

    /**
     * Validates Redis configuration on startup.
     * Fails fast if required environment variables are missing or invalid.
     *
     * @throws IllegalStateException if REDIS_HOST is missing or empty
     * @throws IllegalStateException if REDIS_PORT is not in valid range (1-65535)
     * @throws IllegalStateException if REDIS_DATABASE is not in valid range (0-15)
     */
    @PostConstruct
    public void validateConfiguration() {
        LOG.info("[RedisConfig] Validating Redis configuration...");

        // Fail fast if host is missing
        if (!StringUtils.hasText(redisHost)) {
            throw new IllegalStateException(
                "REDIS_HOST environment variable is required. " +
                "Set it in .env file or environment variables."
            );
        }

        // Validate port range
        if (redisPort < 1 || redisPort > 65535) {
            throw new IllegalStateException(
                "REDIS_PORT must be between 1 and 65535. Current value: " + redisPort
            );
        }

        // Validate database index
        if (redisDatabase < 0 || redisDatabase > 15) {
            throw new IllegalStateException(
                "REDIS_DATABASE must be between 0 and 15. Current value: " + redisDatabase
            );
        }

        LOG.info("[RedisConfig] Redis configured: {}:{} database={}",
            redisHost, redisPort, redisDatabase);
    }

    /**
     * Creates the Redis connection factory with configured settings.
     * Uses Lettuce client for non-blocking I/O.
     *
     * @return configured LettuceConnectionFactory
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        LOG.info("[RedisConfig] Creating Redis connection factory...");

        // Configure Redis server connection
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName(redisHost);
        serverConfig.setPort(redisPort);
        serverConfig.setDatabase(redisDatabase);

        // Set password if provided
        if (StringUtils.hasText(redisPassword)) {
            serverConfig.setPassword(redisPassword);
        }

        // Configure Lettuce client options
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(redisTimeout))
            .shutdownTimeout(Duration.ZERO)
            .build();

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    /**
     * Creates RedisTemplate for generic object serialization.
     * Uses JSON serialization for values, String for keys.
     *
     * @param connectionFactory the Redis connection factory
     * @return configured RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Creates StringRedisTemplate for simple string operations.
     *
     * @param connectionFactory the Redis connection factory
     * @return configured StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * Creates Redis message listener container for pub/sub and keyspace events.
     * Used for handling key expiration events in scheduling.
     *
     * @param connectionFactory the Redis connection factory
     * @return configured RedisMessageListenerContainer
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
