'use client';

import type { Task, DayProgress, DayOfWeek } from '@/types/domain';
import { cn } from '@/lib/utils';
import { getDayOfWeekFromDate } from '@/lib/week/utils';

interface WeekTaskItemProps {
  task: Task;
  day: DayProgress;
  isEditable: boolean;
  onRemove?: (taskId: string, dayOfWeek: DayOfWeek) => void;
}

/**
 * WeekTaskItem component - renders a single task in the week grid.
 * 
 * Displays task description.
 * Provides remove action for editable days.
 * 
 * Visual rules:
 * - Editable: Full color, draggable (future implementation)
 * - Non-editable: Grayed, not draggable
 */
export function WeekTaskItem({ task, day, isEditable, onRemove }: WeekTaskItemProps) {
  const handleRemove = () => {
    if (!isEditable || !onRemove) return;
    const dayOfWeek = getDayOfWeekFromDate(day.date);
    onRemove(task.id, dayOfWeek);
  };

  return (
    <div
      className={cn(
        'p-2 rounded-md border border-border bg-background text-sm',
        !isEditable && 'opacity-60 cursor-not-allowed',
        isEditable && 'hover:border-primary/50 cursor-move'
      )}
      draggable={isEditable}
      onDragStart={(e) => {
        if (!isEditable) {
          e.preventDefault();
          return;
        }
        e.dataTransfer.setData('taskId', task.id);
        e.dataTransfer.setData('sourceDay', day.date);
      }}
    >
      <div className="flex items-start justify-between gap-2">
        <p className="text-sm flex-1 min-w-0 break-words">{task.description}</p>
        {isEditable && (
          <button
            onClick={handleRemove}
            className="flex-shrink-0 text-muted-foreground hover:text-destructive transition-colors"
            aria-label="Remove task"
          >
            <svg
              className="w-4 h-4"
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
          </button>
        )}
      </div>
    </div>
  );
}

export default WeekTaskItem;
