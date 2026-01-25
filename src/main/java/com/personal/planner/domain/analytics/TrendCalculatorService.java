package com.personal.planner.domain.analytics;

import com.personal.planner.domain.common.constants.AnalyticsConstants;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service for calculating trend direction from historical goal snapshots.
 * <p>
 * This service analyzes sequences of {@link GoalSnapshot} records to determine
 * whether goal progress is trending upward, downward, or remaining stable.
 * Trend calculation uses a configurable time window and threshold to distinguish
 * meaningful changes from noise.
 * </p>
 * <p>
 * <b>Trend Calculation Logic:</b>
 * <ul>
 *   <li>Compares the most recent snapshot with a snapshot from the trend window period</li>
 *   <li>Uses {@link AnalyticsConstants#TREND_WINDOW_DAYS} to determine comparison point</li>
 *   <li>Uses {@link AnalyticsConstants#TREND_THRESHOLD} to distinguish meaningful changes</li>
 *   <li>Returns FLAT if insufficient data or change is below threshold</li>
 * </ul>
 * </p>
 * <p>
 * <b>Safety Guarantees:</b>
 * <ul>
 *   <li>Handles null or empty snapshot lists gracefully</li>
 *   <li>Performs bounds checking to prevent index-out-of-bounds exceptions</li>
 *   <li>Handles single snapshot case by returning FLAT trend</li>
 *   <li>Uses safe index calculation: Math.min(windowDays - 1, snapshots.size() - 1)</li>
 * </ul>
 * </p>
 */
@Service
public class TrendCalculatorService {

    /**
     * Enumeration representing the direction of a trend.
     */
    public enum Trend {
        /** Progress is increasing over the trend window */
        UP,
        /** Progress is stable (no significant change) */
        FLAT,
        /** Progress is decreasing over the trend window */
        DOWN
    }

    /**
     * Calculates the trend direction from a sequence of goal snapshots.
     * <p>
     * This method compares the most recent snapshot with a snapshot from the
     * trend window period ago. The comparison uses the configured trend threshold
     * to determine if a change is significant enough to be considered a trend.
     * </p>
     * <p>
     * <b>Input Requirements:</b>
     * <ul>
     *   <li>Snapshots should be ordered by date descending (most recent first)</li>
     *   <li>At least 2 snapshots are required for trend calculation</li>
     * </ul>
     * </p>
     * <p>
     * <b>Edge Cases:</b>
     * <ul>
     *   <li>null or empty list → returns FLAT</li>
     *   <li>Single snapshot → returns FLAT</li>
     *   <li>Insufficient snapshots for window → compares with oldest available</li>
     *   <li>Change below threshold → returns FLAT</li>
     * </ul>
     * </p>
     *
     * @param snapshots list of goal snapshots ordered by date descending (most recent first).
     *                  Can be null or empty.
     * @return the calculated trend direction (UP, FLAT, or DOWN)
     */
    public Trend calculateTrend(List<GoalSnapshot> snapshots) {
        // Handle null or empty snapshots
        if (snapshots == null || snapshots.isEmpty()) {
            return Trend.FLAT;
        }

        // Need at least 2 snapshots to calculate a trend
        if (snapshots.size() < 2) {
            return Trend.FLAT;
        }

        // Get the most recent snapshot (index 0)
        GoalSnapshot latest = snapshots.get(0);

        // Calculate safe index for comparison snapshot
        // Use the smaller of: (windowDays - 1) or (last available index)
        // This ensures we never go out of bounds
        int comparisonIndex = Math.min(AnalyticsConstants.TREND_WINDOW_DAYS - 1, snapshots.size() - 1);
        GoalSnapshot previous = snapshots.get(comparisonIndex);

        // Calculate the change in progress
        double progressChange = latest.getProgress() - previous.getProgress();
        double absoluteChange = Math.abs(progressChange);

        // Only consider it a trend if the change exceeds the threshold
        if (absoluteChange < AnalyticsConstants.TREND_THRESHOLD) {
            return Trend.FLAT;
        }

        // Determine trend direction based on sign of change
        if (progressChange > 0) {
            return Trend.UP;
        } else {
            return Trend.DOWN;
        }
    }
}
