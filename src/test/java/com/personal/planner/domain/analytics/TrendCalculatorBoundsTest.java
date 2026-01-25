package com.personal.planner.domain.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TrendCalculatorService bounds checking and edge cases.
 *
 * <p>Verifies that trend calculation handles edge cases safely:
 * - Empty list returns FLAT
 * - Single snapshot returns FLAT
 * - Safe index handling prevents IndexOutOfBoundsException</p>
 */
class TrendCalculatorBoundsTest {

    private TrendCalculatorService trendCalculator;

    @BeforeEach
    void setUp() {
        trendCalculator = new TrendCalculatorService();
    }

    @Test
    @DisplayName("Empty list should return FLAT trend")
    void emptyListShouldReturnFlat() {
        List<GoalSnapshot> emptyList = Collections.emptyList();
        TrendCalculatorService.Trend result = trendCalculator.calculateTrend(emptyList);
        
        assertEquals(TrendCalculatorService.Trend.FLAT, result);
    }

    @Test
    @DisplayName("Null list should return FLAT trend")
    void nullListShouldReturnFlat() {
        TrendCalculatorService.Trend result = trendCalculator.calculateTrend(null);
        
        assertEquals(TrendCalculatorService.Trend.FLAT, result);
    }

    @Test
    @DisplayName("Single snapshot should return FLAT trend")
    void singleSnapshotShouldReturnFlat() {
        GoalSnapshot snapshot = createSnapshot("goal-1", 0.5, Instant.now());
        List<GoalSnapshot> singleSnapshot = Collections.singletonList(snapshot);
        
        TrendCalculatorService.Trend result = trendCalculator.calculateTrend(singleSnapshot);
        
        assertEquals(TrendCalculatorService.Trend.FLAT, result);
    }

    @Test
    @DisplayName("Two snapshots with insufficient change should return FLAT")
    void twoSnapshotsWithInsufficientChangeShouldReturnFlat() {
        Instant now = Instant.now();
        GoalSnapshot snapshot1 = createSnapshot("goal-1", 0.50, now);
        GoalSnapshot snapshot2 = createSnapshot("goal-1", 0.51, now.plusSeconds(86400)); // 1% change
        
        List<GoalSnapshot> snapshots = List.of(snapshot2, snapshot1); // Most recent first
        
        TrendCalculatorService.Trend result = trendCalculator.calculateTrend(snapshots);
        
        // Change is 1% which is below 2% threshold, should return FLAT
        assertEquals(TrendCalculatorService.Trend.FLAT, result);
    }

    @Test
    @DisplayName("Two snapshots with significant increase should return UP")
    void twoSnapshotsWithSignificantIncreaseShouldReturnUp() {
        Instant now = Instant.now();
        GoalSnapshot snapshot1 = createSnapshot("goal-1", 0.50, now);
        GoalSnapshot snapshot2 = createSnapshot("goal-1", 0.55, now.plusSeconds(86400)); // 5% increase
        
        List<GoalSnapshot> snapshots = List.of(snapshot2, snapshot1); // Most recent first
        
        TrendCalculatorService.Trend result = trendCalculator.calculateTrend(snapshots);
        
        assertEquals(TrendCalculatorService.Trend.UP, result);
    }

    @Test
    @DisplayName("Two snapshots with significant decrease should return DOWN")
    void twoSnapshotsWithSignificantDecreaseShouldReturnDown() {
        Instant now = Instant.now();
        GoalSnapshot snapshot1 = createSnapshot("goal-1", 0.50, now);
        GoalSnapshot snapshot2 = createSnapshot("goal-1", 0.45, now.plusSeconds(86400)); // 5% decrease
        
        List<GoalSnapshot> snapshots = List.of(snapshot2, snapshot1); // Most recent first
        
        TrendCalculatorService.Trend result = trendCalculator.calculateTrend(snapshots);
        
        assertEquals(TrendCalculatorService.Trend.DOWN, result);
    }

    @Test
    @DisplayName("Many snapshots should use safe index calculation")
    void manySnapshotsShouldUseSafeIndexCalculation() {
        // Create 3 snapshots (less than TREND_WINDOW_DAYS which is 7)
        Instant now = Instant.now();
        GoalSnapshot snapshot1 = createSnapshot("goal-1", 0.50, now);
        GoalSnapshot snapshot2 = createSnapshot("goal-1", 0.55, now.plusSeconds(86400));
        GoalSnapshot snapshot3 = createSnapshot("goal-1", 0.60, now.plusSeconds(172800));
        
        List<GoalSnapshot> snapshots = List.of(snapshot3, snapshot2, snapshot1); // Most recent first
        
        // Should not throw IndexOutOfBoundsException
        assertDoesNotThrow(() -> {
            TrendCalculatorService.Trend result = trendCalculator.calculateTrend(snapshots);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("More snapshots than window should compare with window index")
    void moreSnapshotsThanWindowShouldCompareWithWindowIndex() {
        // Create 10 snapshots (more than TREND_WINDOW_DAYS which is 7)
        List<GoalSnapshot> snapshots = new ArrayList<>();
        Instant baseTime = Instant.now();
        
        for (int i = 0; i < 10; i++) {
            double progress = 0.50 + (i * 0.01); // Increasing progress
            snapshots.add(createSnapshot("goal-1", progress, baseTime.plusSeconds(i * 86400)));
        }
        
        Collections.reverse(snapshots); // Most recent first
        
        // Should not throw IndexOutOfBoundsException
        // Should compare index 0 with index 6 (TREND_WINDOW_DAYS - 1)
        assertDoesNotThrow(() -> {
            TrendCalculatorService.Trend result = trendCalculator.calculateTrend(snapshots);
            assertNotNull(result);
        });
    }

    /**
     * Helper method to create a GoalSnapshot for testing.
     */
    private GoalSnapshot createSnapshot(String goalId, double progress, Instant timestamp) {
        return GoalSnapshot.builder()
                .goalId(goalId)
                .progress(progress)
                .snapshottedAt(timestamp)
                .build();
    }
}
