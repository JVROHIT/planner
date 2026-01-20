package com.personal.planner.domain.analytics;

import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service for extracting meaning from historical facts.
 * <p>
 * Constraints:
 * - MUST NEVER modify the facts it interprets (GoalSnapshots).
 * - MUST ONLY provide interpretive metrics like velocity or momentum.
 * </p>
 */
@Service
public class TrendCalculatorService {

    /**
     * Trend classification for goal progress trajectory.
     */
    public enum Trend {
        UP, FLAT, DOWN
    }

    /**
     * "Pure function."
     * "No database access."
     * "No side effects."
     * <p>
     * Calculates the statistical trend from a sequence of snapshots.
     * </p>
     */
    public Trend calculateTrend(List<GoalSnapshot> snapshots, int windowDays) {
        // Pure function stub
        return Trend.FLAT;
    }

    public void calculateTrend(String goalId) {
        // Method stub for interpreting snapshot sequences
    }
}
