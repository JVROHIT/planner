package com.personal.planner.domain.common.constants;

/**
 * Analytics calculation constants for FocusFlow.
 * <p>
 * These constants define the behavior of analytics calculations including:
 * - Trend analysis window and thresholds
 * - Performance metrics calculations
 * - Statistical analysis parameters
 * </p>
 */
public final class AnalyticsConstants {

    private AnalyticsConstants() {
        // Prevent instantiation
    }

    /**
     * Number of days to look back for trend calculation.
     * <p>
     * Used to determine the time window for analyzing completion rate trends.
     * A 7-day window balances between:
     * - Responsiveness to recent behavior changes
     * - Stability against daily fluctuations
     * </p>
     */
    public static final int TREND_WINDOW_DAYS = 7;

    /**
     * Threshold for trend direction determination (2%).
     * <p>
     * Completion rate changes below this threshold are considered "stable".
     * Changes above this threshold indicate "improving" or "declining" trends.
     * </p>
     * <p>
     * Example: If completion rate changes from 70% to 71%, the change is 1.4%,
     * which is below the 2% threshold, so the trend is "stable".
     * </p>
     */
    public static final double TREND_THRESHOLD = 0.02;
}
