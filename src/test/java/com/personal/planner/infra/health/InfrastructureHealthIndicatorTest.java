package com.personal.planner.infra.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for infrastructure health check.
 *
 * <p>Verifies that InfrastructureHealthIndicator correctly reports
 * health status for MongoDB and Redis connectivity.</p>
 */
class InfrastructureHealthIndicatorTest {

    private InfrastructureHealthIndicator healthIndicator;
    private MongoTemplate mongoTemplate;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        healthIndicator = new InfrastructureHealthIndicator(mongoTemplate, redisTemplate);
    }

    @Test
    @DisplayName("Should report healthy when MongoDB and Redis are connected")
    void shouldReportHealthyWhenConnected() {
        // Mock successful MongoDB ping
        when(mongoTemplate.executeCommand(anyString())).thenReturn(null);
        
        // Mock successful Redis write/read
        doNothing().when(valueOperations).set(anyString(), anyString());
        when(valueOperations.get(anyString())).thenReturn("ok");

        Health health = healthIndicator.health();

        assertNotNull(health);
        assertEquals(Status.UP, health.getStatus());
        assertTrue(health.getDetails().containsKey("mongodb"));
        assertTrue(health.getDetails().containsKey("redis"));
        assertEquals("Connected", health.getDetails().get("mongodb"));
        assertEquals("Connected", health.getDetails().get("redis"));
        
        verify(mongoTemplate).executeCommand(anyString());
        verify(valueOperations).set(anyString(), anyString());
        verify(valueOperations).get(anyString());
    }

    @Test
    @DisplayName("Should report DOWN when MongoDB is disconnected")
    void shouldReportDownWhenMongoDisconnected() {
        // Mock MongoDB failure
        when(mongoTemplate.executeCommand(anyString())).thenThrow(new RuntimeException("Connection failed"));
        
        // Mock successful Redis
        doNothing().when(valueOperations).set(anyString(), anyString());
        when(valueOperations.get(anyString())).thenReturn("ok");

        Health health = healthIndicator.health();

        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("mongodb"));
        assertTrue(health.getDetails().get("mongodb").toString().contains("Disconnected"));
        
        verify(mongoTemplate).executeCommand(anyString());
        verify(valueOperations).set(anyString(), anyString());
        verify(valueOperations).get(anyString());
    }

    @Test
    @DisplayName("Should report DOWN when Redis is disconnected")
    void shouldReportDownWhenRedisDisconnected() {
        // Mock successful MongoDB
        when(mongoTemplate.executeCommand(anyString())).thenReturn(null);
        
        // Mock Redis failure on set operation
        doThrow(new RuntimeException("Connection failed")).when(valueOperations).set(anyString(), anyString());

        Health health = healthIndicator.health();

        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("redis"));
        assertTrue(health.getDetails().get("redis").toString().contains("Disconnected"));
        
        verify(mongoTemplate).executeCommand(anyString());
        verify(valueOperations).set(anyString(), anyString());
    }
}
