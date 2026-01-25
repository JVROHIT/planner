'use client';

/**
 * GoalProgress Component
 * 
 * Visual progress bar showing actual vs expected progress.
 * Renders backend-provided percentages - never computes client-side.
 * 
 * Philosophy: Display meaning, don't derive it.
 */

interface GoalProgressProps {
    actualPercent: number;
    expectedPercent: number;
    status: 'AHEAD' | 'ON_TRACK' | 'BEHIND';
}

export function GoalProgress({ actualPercent, expectedPercent, status }: GoalProgressProps) {
    // Status-based color mapping
    const statusColors = {
        AHEAD: 'bg-green-500',
        ON_TRACK: 'bg-blue-500',
        BEHIND: 'bg-orange-500',
    };

    const barColor = statusColors[status];

    return (
        <div className="space-y-2">
            {/* Progress bar */}
            <div className="relative h-3 bg-muted rounded-full overflow-hidden">
                {/* Expected progress (lighter background) */}
                <div
                    className="absolute h-full bg-muted-foreground/20 transition-all duration-300"
                    style={{ width: `${Math.min(expectedPercent, 100)}%` }}
                />

                {/* Actual progress (colored) */}
                <div
                    className={`absolute h-full ${barColor} transition-all duration-300`}
                    style={{ width: `${Math.min(actualPercent, 100)}%` }}
                />
            </div>

            {/* Labels */}
            <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">
                    Actual: <span className="font-medium text-foreground">{actualPercent.toFixed(1)}%</span>
                </span>
                <span className="text-muted-foreground">
                    Expected: <span className="font-medium text-foreground">{expectedPercent.toFixed(1)}%</span>
                </span>
            </div>
        </div>
    );
}

export default GoalProgress;
