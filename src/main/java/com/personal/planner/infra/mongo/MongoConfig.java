package com.personal.planner.infra.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * MongoDB configuration for FocusFlow.
 *
 * <p>All connection settings are driven by environment variables:
 * <ul>
 *   <li>MONGODB_URI: Full connection URI</li>
 *   <li>MONGODB_DATABASE: Database name</li>
 * </ul>
 * </p>
 *
 * <p>This configuration validates required settings on startup
 * and fails fast if MongoDB is not properly configured.</p>
 *
 * <p>Connection pool settings:
 * <ul>
 *   <li>Max connections: 50</li>
 *   <li>Min connections: 5</li>
 *   <li>Max wait time: 30 seconds</li>
 *   <li>Connect timeout: 10 seconds</li>
 *   <li>Read timeout: 30 seconds</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.personal.planner.infra.mongo")
public class MongoConfig extends AbstractMongoClientConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    /**
     * Validates MongoDB configuration on startup.
     * Fails fast if required environment variables are missing.
     *
     * @throws IllegalStateException if MONGODB_URI is missing or empty
     * @throws IllegalStateException if MONGODB_DATABASE is missing or empty
     */
    @PostConstruct
    public void validateConfiguration() {
        LOG.info("[MongoConfig] Validating MongoDB configuration...");

        // Fail fast if URI is missing or empty
        if (!StringUtils.hasText(mongoUri)) {
            throw new IllegalStateException(
                "MONGODB_URI environment variable is required. " +
                "Set it in .env file or environment variables."
            );
        }

        // Fail fast if database name is missing
        if (!StringUtils.hasText(databaseName)) {
            throw new IllegalStateException(
                "MONGODB_DATABASE environment variable is required. " +
                "Set it in .env file or environment variables."
            );
        }

        LOG.info("[MongoConfig] MongoDB configured for database: {}", databaseName);
    }

    /**
     * Returns the database name for MongoDB operations.
     *
     * @return the configured database name
     */
    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    /**
     * Creates and configures the MongoDB client.
     * Includes connection pooling and timeout settings.
     *
     * @return configured MongoClient instance
     */
    @Override
    public MongoClient mongoClient() {
        LOG.info("[MongoConfig] Creating MongoDB client...");

        ConnectionString connectionString = new ConnectionString(mongoUri);

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            // Connection pool settings
            .applyToConnectionPoolSettings(builder -> builder
                .maxSize(50)                    // Max connections in pool
                .minSize(5)                     // Min connections to maintain
                .maxWaitTime(30, TimeUnit.SECONDS)
            )
            // Socket settings
            .applyToSocketSettings(builder -> builder
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
            )
            // Server selection timeout
            .applyToClusterSettings(builder -> builder
                .serverSelectionTimeout(30, TimeUnit.SECONDS)
            )
            .build();

        return MongoClients.create(settings);
    }

    /**
     * Configures custom type conversions for MongoDB.
     * Add converters here for custom types (e.g., ZonedDateTime).
     *
     * @return custom conversions configuration
     */
    @Bean
    public MongoCustomConversions customConversions() {
        // Add custom converters if needed (e.g., for LocalDateTime with timezone)
        return new MongoCustomConversions(Collections.emptyList());
    }
}
