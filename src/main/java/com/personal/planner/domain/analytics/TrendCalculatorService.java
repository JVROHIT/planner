package com.personal.planner.domain.analytics;

import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service for extracting meaning from historical facts.
 */
@Service
public class TrendCalculatorService {

    public enum Trend {
        UP, FLAT, DOWN
    }

    /**
     * Calculates the statistical trend from a sequence of snapshots.
     */
    public Trend calculateTrend(List<GoalSnapshot> snapshots, int windowDays) {
        if (snapshots == null || snapshots.size() < 2) {
            return Trend.FLAT;
        }

        // Snapshots are usually ordered by date desc in Repository
        GoalSnapshot latest = snapshots.get(0);
        GoalSnapshot previous = snapshots.get(1);

        if (latest.getProgress() > previous.getProgress()) {
            return Trend.UP;
        } else if (latest.getProgress() < previous.getProgress()) {
            return Trend.DOWN;
        } else {
            return Trend.FLAT;
        }
    }
}
