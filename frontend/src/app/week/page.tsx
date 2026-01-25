'use client';

import { useState } from 'react';
import { AppShell } from '@/components/layout';
import { useWeekDashboard, useWeeklyPlan } from '@/hooks';
import { WeeklyGrid } from '@/components/week';
import { ApiError } from '@/components/error/ApiError';
import { getWeekStart, getPreviousWeek, getNextWeek, getToday } from '@/lib/week/utils';
import { cn } from '@/lib/utils';

/**
 * Week page - Intent Mode.
 * "What you plan to do this week."
 *
 * Source of truth: WeeklyPlan via /api/weekly-plan/{date}
 * Progress data: DayProgress[] via /api/dashboard/week
 * 
 * Only future days are editable.
 * Past/closed days are frozen.
 */
export default function WeekPage() {
  const [currentWeekStart, setCurrentWeekStart] = useState(() => getWeekStart(getToday()));

  const {
    data: weekProgress,
    isLoading: isLoadingProgress,
    error: progressError,
    refetch: refetchProgress
  } = useWeekDashboard(currentWeekStart);

  const {
    data: weeklyPlan,
    isLoading: isLoadingPlan,
    error: planError,
    refetch: refetchPlan
  } = useWeeklyPlan(currentWeekStart);

  const isLoading = isLoadingProgress || isLoadingPlan;
  const error = progressError || planError;

  const handleReset = () => {
    refetchProgress();
    refetchPlan();
  };

  const handlePreviousWeek = () => {
    setCurrentWeekStart(getPreviousWeek(currentWeekStart));
  };

  const handleNextWeek = () => {
    setCurrentWeekStart(getNextWeek(currentWeekStart));
  };

  const handleCurrentWeek = () => {
    setCurrentWeekStart(getWeekStart(getToday()));
  };

  const isCurrentWeek = currentWeekStart === getWeekStart(getToday());

  return (
    <AppShell>
      <div className="max-w-7xl mx-auto p-6 space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold">Week</h1>
            <p className="text-muted-foreground mt-1">
              Intent mode - What you plan to do
            </p>
          </div>

          {/* Week navigation */}
          <div className="flex items-center gap-2">
            <button
              onClick={handlePreviousWeek}
              className="px-3 py-2 text-sm font-medium rounded-md border border-border hover:bg-muted transition-colors"
            >
              ← Previous
            </button>
            <button
              onClick={handleCurrentWeek}
              disabled={isCurrentWeek}
              className={cn(
                'px-3 py-2 text-sm font-medium rounded-md border border-border transition-colors',
                isCurrentWeek
                  ? 'opacity-50 cursor-not-allowed'
                  : 'hover:bg-muted'
              )}
            >
              Current Week
            </button>
            <button
              onClick={handleNextWeek}
              className="px-3 py-2 text-sm font-medium rounded-md border border-border hover:bg-muted transition-colors"
            >
              Next →
            </button>
          </div>
        </div>

        {/* Error state */}
        {error && (
          <ApiError error={error} reset={handleReset} />
        )}

        {/* Week range display */}
        <div className="text-sm text-muted-foreground">
          {new Date(currentWeekStart).toLocaleDateString('en-US', {
            month: 'long',
            day: 'numeric',
            year: 'numeric',
          })}{' '}
          -{' '}
          {(() => {
            const weekEndDate = new Date(getNextWeek(currentWeekStart));
            weekEndDate.setDate(weekEndDate.getDate() - 1);
            return weekEndDate.toLocaleDateString('en-US', {
              month: 'long',
              day: 'numeric',
              year: 'numeric',
            });
          })()}
        </div>

        {/* Week grid */}
        <WeeklyGrid
          weekProgress={weekProgress}
          weeklyPlan={weeklyPlan}
          weekStart={currentWeekStart}
          isLoading={isLoading}
        />
      </div>
    </AppShell>
  );
}
