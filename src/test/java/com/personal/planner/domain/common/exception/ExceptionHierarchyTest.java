package com.personal.planner.domain.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for exception hierarchy and message preservation.
 *
 * <p>Verifies that all domain exceptions properly extend FocusFlowException
 * and preserve exception messages correctly.</p>
 */
class ExceptionHierarchyTest {

    @Test
    @DisplayName("All exceptions should extend FocusFlowException")
    void allExceptionsExtendFocusFlowException() {
        // Verify inheritance hierarchy
        assertTrue(new TaskNotFoundException("test") instanceof FocusFlowException);
        assertTrue(new GoalNotFoundException("test") instanceof FocusFlowException);
        assertTrue(new KeyResultNotFoundException("test") instanceof FocusFlowException);
        assertTrue(new DailyPlanNotFoundException("test") instanceof FocusFlowException);
        assertTrue(new WeeklyPlanNotFoundException("test") instanceof FocusFlowException);
        assertTrue(new UserNotFoundException("test") instanceof FocusFlowException);
        assertTrue(new AuthorizationException("test") instanceof FocusFlowException);
        assertTrue(new AuthenticationException("test") instanceof FocusFlowException);
        assertTrue(new InvalidRequestException("test") instanceof FocusFlowException);
        assertTrue(new DomainViolationException("test") instanceof FocusFlowException);
        assertTrue(new EventProcessingException("test") instanceof FocusFlowException);
    }

    @Test
    @DisplayName("EntityNotFoundException subclasses should extend EntityNotFoundException")
    void entityNotFoundSubclassesExtendEntityNotFoundException() {
        assertTrue(new TaskNotFoundException("test") instanceof EntityNotFoundException);
        assertTrue(new GoalNotFoundException("test") instanceof EntityNotFoundException);
        assertTrue(new KeyResultNotFoundException("test") instanceof EntityNotFoundException);
        assertTrue(new DailyPlanNotFoundException("test") instanceof EntityNotFoundException);
        assertTrue(new WeeklyPlanNotFoundException("test") instanceof EntityNotFoundException);
        assertTrue(new UserNotFoundException("test") instanceof EntityNotFoundException);
    }

    @Test
    @DisplayName("Exception messages should be preserved")
    void exceptionMessagesArePreserved() {
        String message = "Test exception message";
        String entityId = "entity-123";
        
        // EntityNotFoundException subclasses take entityId and construct message
        assertNotNull(new TaskNotFoundException(entityId).getMessage());
        assertNotNull(new GoalNotFoundException(entityId).getMessage());
        assertNotNull(new KeyResultNotFoundException(entityId).getMessage());
        assertNotNull(new DailyPlanNotFoundException(entityId).getMessage());
        assertNotNull(new WeeklyPlanNotFoundException(entityId).getMessage());
        assertNotNull(new UserNotFoundException(entityId).getMessage());
        
        // Other exceptions take message directly
        assertEquals(message, new AuthorizationException(message).getMessage());
        assertEquals(message, new AuthenticationException(message).getMessage());
        assertEquals(message, new InvalidRequestException(message).getMessage());
        assertEquals(message, new DomainViolationException(message).getMessage());
        assertEquals(message, new EventProcessingException(message).getMessage());
    }

    @Test
    @DisplayName("Exception messages can be null for direct message exceptions")
    void exceptionMessagesCanBeNull() {
        // AuthorizationException and others that take message directly can have null
        assertNull(new AuthorizationException(null).getMessage());
        assertNull(new AuthenticationException(null).getMessage());
        assertNull(new InvalidRequestException(null).getMessage());
        assertNull(new DomainViolationException(null).getMessage());
        assertNull(new EventProcessingException(null).getMessage());
        
        // EntityNotFoundException subclasses construct messages, so null entityId
        // will result in a constructed message, not null
        assertNotNull(new TaskNotFoundException((String) null).getMessage());
    }
}
