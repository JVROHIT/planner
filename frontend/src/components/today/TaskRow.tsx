'use client';

import { useCompleteTask, useMissTask } from '@/hooks';
import type { TaskExecution } from '@/types/domain';
import { cn } from '@/lib/utils';

interface TaskRowProps {
  task: TaskExecution;
  date: string; // ISO date (YYYY-MM-DD)
  dayClosed: boolean;
}

/**
 * TaskRow component - renders a single task in the Today view.
 * 
 * Displays task title and status (pending/completed/missed).
 * Provides Complete/Miss actions when day is not closed.
 * 
 * Visual rules:
 * - Closed day: Frozen (grayed, no actions)
 * - Completed: Success styling
 * - Missed: Muted styling
 * - Pending: Active styling
 */
export function TaskRow({ task, date, dayClosed }: TaskRowProps) {
  const { mutateAsync: completeTask, isPending: isCompleting } = useCompleteTask(date);
  const { mutateAsync: missTask, isPending: isMissing } = useMissTask(date);

  const isPending = !task.completed && !task.missed;
  const isLoading = isCompleting || isMissing;

  const handleComplete = async () => {
    if (dayClosed || isLoading) return;
    try {
      await completeTask({ taskId: task.taskId, date });
    } catch {
      // Error handled by hook
    }
  };

  const handleMiss = async () => {
    if (dayClosed || isLoading) return;
    try {
      await missTask({ taskId: task.taskId, date });
    } catch {
      // Error handled by hook
    }
  };

  return (
    <div
      className={cn(
        'flex items-center gap-3 p-3 rounded-lg border transition-colors',
        dayClosed && 'opacity-60 cursor-not-allowed',
        task.completed && 'bg-success/10 border-success/20',
        task.missed && 'bg-muted/50 border-muted',
        isPending && !dayClosed && 'bg-background border-border hover:border-primary/50'
      )}
    >
      {/* Status indicator */}
      <div
        className={cn(
          'w-5 h-5 rounded-full border-2 flex-shrink-0 flex items-center justify-center',
          task.completed && 'bg-success border-success',
          task.missed && 'bg-muted border-muted',
          isPending && 'border-border'
        )}
      >
        {task.completed && (
          <svg
            className="w-3 h-3 text-white"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M5 13l4 4L19 7"
            />
          </svg>
        )}
        {task.missed && (
          <svg
            className="w-3 h-3 text-muted-foreground"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        )}
      </div>

      {/* Task title */}
      <div className="flex-1 min-w-0">
        <p
          className={cn(
            'text-sm font-medium truncate',
            task.completed && 'text-success-foreground line-through',
            task.missed && 'text-muted-foreground',
            isPending && 'text-foreground'
          )}
        >
          {task.task.description}
        </p>
      </div>

      {/* Actions */}
      {!dayClosed && isPending && (
        <div className="flex gap-2 flex-shrink-0">
          <button
            onClick={handleComplete}
            disabled={isLoading}
            aria-label={`Mark "${task.task.description}" as complete`}
            className={cn(
              'px-3 py-1 text-xs font-medium rounded-md transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-success',
              'bg-success text-success-foreground hover:bg-success/90',
              'disabled:opacity-50 disabled:cursor-not-allowed'
            )}
          >
            {isCompleting ? '...' : 'Complete'}
          </button>
          <button
            onClick={handleMiss}
            disabled={isLoading}
            aria-label={`Mark "${task.task.description}" as missed`}
            className={cn(
              'px-3 py-1 text-xs font-medium rounded-md transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-muted-foreground',
              'bg-muted text-muted-foreground hover:bg-muted/80',
              'disabled:opacity-50 disabled:cursor-not-allowed'
            )}
          >
            {isMissing ? '...' : 'Miss'}
          </button>
        </div>
      )}
    </div>
  );
}

export default TaskRow;
