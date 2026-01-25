'use client';

import { TaskRow } from './TaskRow';
import type { DailyPlan } from '@/types/domain';

interface TodayTaskListProps {
  dailyPlan: DailyPlan | null;
  isLoading: boolean;
}

/**
 * TodayTaskList component - renders the list of tasks for today.
 * 
 * Shows loading skeleton while loading.
 * Shows empty state when no tasks.
 * Renders TaskRow for each task.
 */
export function TodayTaskList({ dailyPlan, isLoading }: TodayTaskListProps) {
  if (isLoading) {
    return (
      <div className="space-y-3">
        {[1, 2, 3].map((i) => (
          <div
            key={i}
            className="h-16 bg-muted/30 rounded-lg animate-pulse"
            aria-label="Loading task"
          />
        ))}
      </div>
    );
  }

  if (!dailyPlan || dailyPlan.tasks.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground text-sm">No tasks for today</p>
        <p className="text-xs text-muted-foreground mt-1">
          Tasks from your weekly plan will appear here
        </p>
      </div>
    );
  }

  const date = dailyPlan.day;

  return (
    <div className="space-y-3">
      {dailyPlan.tasks.map((task) => (
        <TaskRow
          key={task.taskId}
          task={task}
          date={date}
          dayClosed={dailyPlan.closed}
        />
      ))}
    </div>
  );
}

export default TodayTaskList;
