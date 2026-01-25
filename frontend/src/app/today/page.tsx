'use client';

import { AppShell } from '@/components/layout';
import { useTodayDashboard } from '@/hooks';
import { TodaySummary, TodayTaskList, StreakBadge, GoalSummaryCard } from '@/components/today';
import { ApiError } from '@/components/error/ApiError';

/**
 * Today page - Execution Mode.
 * "What you're actually doing today."
 *
 * Source of truth: DailyPlan(today) via /api/dashboard/today
 * 
 * This is the SINGLE authoritative query for the Today screen.
 * Never reads from Task directly - all data comes from DailyPlan.
 */
export default function TodayPage() {
  const { data: dashboard, isLoading, error, refetch } = useTodayDashboard();

  return (
    <AppShell>
      <div className="max-w-4xl mx-auto p-6 space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold">Today</h1>
          <p className="text-muted-foreground mt-1">
            Execution mode - What you&apos;re actually doing
          </p>
        </div>

        {/* Error state */}
        {error && (
          <ApiError error={error} reset={refetch} />
        )}

        {/* Summary and widgets */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="md:col-span-2">
            <TodaySummary dashboard={dashboard} isLoading={isLoading} />
          </div>
          <div className="space-y-4">
            {dashboard && <StreakBadge streak={dashboard.currentStreak} />}
            {dashboard && dashboard.goalSummaries.length > 0 && (
              <div className="space-y-2">
                <h3 className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                  Goals
                </h3>
                {dashboard.goalSummaries.slice(0, 3).map((summary) => (
                  <GoalSummaryCard key={summary.goalId} summary={summary} />
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Task list */}
        <div>
          <h2 className="text-lg font-semibold mb-4">Tasks</h2>
          <TodayTaskList
            dailyPlan={dashboard?.todayPlan ?? null}
            isLoading={isLoading}
          />
        </div>
      </div>
    </AppShell>
  );
}
