package com.personal.planner.domain.common.exception;

/**
 * Exception thrown when a WeeklyPlan entity cannot be found by its identifier.
 *
 * <p>This exception should be thrown when:</p>
 * <ul>
 *   <li>Attempting to retrieve a weekly plan by ID that does not exist</li>
 *   <li>Attempting to retrieve a weekly plan for a specific week that does not exist</li>
 *   <li>Attempting to update or delete a non-existent weekly plan</li>
 *   <li>Attempting to add daily plans to a non-existent weekly plan</li>
 *   <li>Attempting to access a weekly plan that has been deleted</li>
 * </ul>
 *
 * @author FocusFlow Team
 * @since 1.0
 */
public class WeeklyPlanNotFoundException extends EntityNotFoundException {

    /**
     * Constructs a new weekly plan not found exception with the specified entity type and weekly plan identifier.
     *
     * @param entityType the type of entity (e.g., "WeeklyPlan")
     * @param entityId the identifier of the weekly plan that was not found
     */
    public WeeklyPlanNotFoundException(String entityType, String entityId) {
        super(entityType, entityId);
    }

    /**
     * Constructs a new weekly plan not found exception with the specified weekly plan identifier.
     *
     * @param weeklyPlanId the identifier of the weekly plan that was not found
     */
    public WeeklyPlanNotFoundException(String weeklyPlanId) {
        super("WeeklyPlan", weeklyPlanId);
    }
}
