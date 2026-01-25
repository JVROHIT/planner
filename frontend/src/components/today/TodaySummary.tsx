'use client';

import type { TodayDashboard } from '@/types/domain';
import { cn } from '@/lib/utils';
import { Skeleton } from '@/components/ui/Skeleton';

interface TodaySummaryProps {
  dashboard: TodayDashboard | undefined;
  isLoading: boolean;
}

/**
 * TodaySummary component - displays completion statistics.
 * 
 * Shows:
 * - Completion count (completed / total)
 * - Completion percentage
 * - Current streak
 * 
 * All data comes from backend - no client-side computation.
 */
export function TodaySummary({ dashboard, isLoading }: TodaySummaryProps) {
  if (isLoading) {
    return (
      <div className="bg-card border border-border rounded-lg p-4 space-y-4">
        <div className="flex justify-between items-center">
          <Skeleton className="h-4 w-24" />
          <Skeleton className="h-4 w-12" />
        </div>
        <div className="space-y-2">
          <Skeleton className="h-8 w-16" />
          <Skeleton className="h-2 w-full" />
          <Skeleton className="h-3 w-20" />
        </div>
      </div>
    );
  }

  if (!dashboard) {
    return null;
  }

  const todayPlan = dashboard.todayPlan;
  const total = todayPlan?.tasks.length ?? 0;
  const completed = todayPlan?.tasks.filter((t) => t.completed).length ?? 0;
  const ratio = dashboard.completionRatio; // From backend, not computed
  const streak = dashboard.currentStreak; // From backend, not computed

  return (
    <div className="bg-background border border-border rounded-lg p-4">
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-medium text-muted-foreground">Today&apos;s Progress</h3>
        {todayPlan?.closed && (
          <span className="text-xs text-muted-foreground bg-muted px-2 py-1 rounded">
            Closed
          </span>
        )}
      </div>

      <div className="space-y-3">
        {/* Completion count */}
        <div>
          <div className="flex items-baseline gap-2 mb-1">
            <span className="text-2xl font-bold">{completed}</span>
            <span className="text-sm text-muted-foreground">/ {total}</span>
          </div>
          <div className="w-full bg-muted rounded-full h-2 overflow-hidden">
            <div
              className={cn(
                'h-full transition-all duration-300',
                ratio === 1 ? 'bg-success' : ratio >= 0.5 ? 'bg-primary' : 'bg-warning'
              )}
              style={{ width: `${ratio * 100}%` }}
              role="progressbar"
              aria-valuenow={ratio * 100}
              aria-valuemin={0}
              aria-valuemax={100}
            />
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            {Math.round(ratio * 100)}% complete
          </p>
        </div>

        {/* Streak */}
        <div className="flex items-center gap-2 pt-2 border-t border-border">
          <span className="text-xs text-muted-foreground">Current streak:</span>
          <span className="text-sm font-semibold">{streak} days</span>
        </div>
      </div>
    </div>
  );
}

export default TodaySummary;
