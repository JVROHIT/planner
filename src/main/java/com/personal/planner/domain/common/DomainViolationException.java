package com.personal.planner.domain.common;

/**
 * Exception thrown when an architectural invariant or domain law is violated.
 * <p>
 * Examples:
 * - Attempting to mutate a closed DailyPlan.
 * - Attempting to rewrite historical facts.
 * - Circular dependency between intent and truth.
 * </p>
 */
public class DomainViolationException extends RuntimeException {
    public DomainViolationException(String message) {
        super(message);
    }
}
