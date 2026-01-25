'use client';

import { cn } from '@/lib/utils';

interface StreakBadgeProps {
  streak: number;
}

/**
 * StreakBadge component - displays current streak count.
 * 
 * Renders backend-provided streak value.
 * No client-side computation.
 * 
 * Visual indicator: Fire icon for active streak.
 */
export function StreakBadge({ streak }: StreakBadgeProps) {
  return (
    <div className="bg-background border border-border rounded-lg p-4">
      <div className="flex items-center gap-3">
        <div
          className={cn(
            'w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0',
            streak > 0 ? 'bg-warning/20' : 'bg-muted/30'
          )}
        >
          {streak > 0 ? (
            <svg
              className="w-6 h-6 text-warning"
              fill="currentColor"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path d="M17.657 18.657A8 8 0 016.343 7.343L7.757 5.93A6 6 0 1019.071 17.243l1.414-1.414zM12 2a10 10 0 11-4.95 18.95L12 22l4.95-1.05A10 10 0 0112 2z" />
            </svg>
          ) : (
            <svg
              className="w-6 h-6 text-muted-foreground"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          )}
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-xs text-muted-foreground">Current Streak</p>
          <p className="text-lg font-bold">
            {streak} {streak === 1 ? 'day' : 'days'}
          </p>
        </div>
      </div>
    </div>
  );
}

export default StreakBadge;
