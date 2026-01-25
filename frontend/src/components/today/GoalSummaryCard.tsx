'use client';

import Link from 'next/link';
import type { GoalSummary } from '@/types/domain';
import { cn } from '@/lib/utils';

interface GoalSummaryCardProps {
  summary: GoalSummary;
}

/**
 * GoalSummaryCard component - displays a goal summary with progress.
 * 
 * Renders backend-provided progress data.
 * No client-side computation of progress.
 * Links to goals page for details.
 */
export function GoalSummaryCard({ summary }: GoalSummaryCardProps) {
  const progress = summary.averageProgress; // From backend, not computed

  return (
    <Link
      href="/goals"
      className="block bg-background border border-border rounded-lg p-3 hover:border-primary/50 transition-colors"
    >
      <div className="space-y-2">
        {/* Goal title */}
        <p className="text-sm font-medium truncate">{summary.title}</p>

        {/* Progress bar */}
        <div className="space-y-1">
          <div className="flex items-center justify-between text-xs">
            <span className="text-muted-foreground">Progress</span>
            <span className="font-medium">{Math.round(progress * 100)}%</span>
          </div>
          <div className="w-full bg-muted rounded-full h-1.5 overflow-hidden">
            <div
              className={cn(
                'h-full transition-all',
                progress === 1
                  ? 'bg-success'
                  : progress >= 0.7
                  ? 'bg-primary'
                  : progress >= 0.4
                  ? 'bg-warning'
                  : 'bg-destructive'
              )}
              style={{ width: `${progress * 100}%` }}
              role="progressbar"
              aria-valuenow={progress * 100}
              aria-valuemin={0}
              aria-valuemax={100}
            />
          </div>
        </div>

        {/* Status indicator */}
        <div className="flex items-center gap-2 text-xs">
          <span
            className={cn(
              'px-2 py-0.5 rounded',
              summary.status === 'AHEAD' && 'bg-success/20 text-success',
              summary.status === 'ON_TRACK' && 'bg-primary/20 text-primary',
              summary.status === 'BEHIND' && 'bg-destructive/20 text-destructive'
            )}
          >
            {summary.status === 'AHEAD' && 'Ahead'}
            {summary.status === 'ON_TRACK' && 'On track'}
            {summary.status === 'BEHIND' && 'Behind'}
          </span>
          {summary.trend === 'UP' && (
            <span className="text-success" aria-label="Trending up">
              ↑
            </span>
          )}
          {summary.trend === 'DOWN' && (
            <span className="text-destructive" aria-label="Trending down">
              ↓
            </span>
          )}
          {summary.trend === 'FLAT' && (
            <span className="text-muted-foreground" aria-label="Trending flat">
              →
            </span>
          )}
        </div>
      </div>
    </Link>
  );
}

export default GoalSummaryCard;
