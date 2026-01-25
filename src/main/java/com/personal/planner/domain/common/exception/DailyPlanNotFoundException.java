package com.personal.planner.domain.common.exception;

/**
 * Exception thrown when a DailyPlan entity cannot be found by its identifier.
 *
 * <p>This exception should be thrown when:</p>
 * <ul>
 *   <li>Attempting to retrieve a daily plan by ID that does not exist</li>
 *   <li>Attempting to retrieve a daily plan for a specific date that does not exist</li>
 *   <li>Attempting to update or delete a non-existent daily plan</li>
 *   <li>Attempting to add tasks to a non-existent daily plan</li>
 *   <li>Attempting to access a daily plan that has been deleted</li>
 * </ul>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
public class DailyPlanNotFoundException extends EntityNotFoundException {

    /**
     * Constructs a new daily plan not found exception with the specified entity type and daily plan identifier.
     *
     * @param entityType the type of entity (e.g., "DailyPlan")
     * @param entityId the identifier of the daily plan that was not found
     */
    public DailyPlanNotFoundException(String entityType, String entityId) {
        super(entityType, entityId);
    }

    /**
     * Constructs a new daily plan not found exception with the specified daily plan identifier.
     *
     * @param dailyPlanId the identifier of the daily plan that was not found
     */
    public DailyPlanNotFoundException(String dailyPlanId) {
        super("DailyPlan", dailyPlanId);
    }
}
