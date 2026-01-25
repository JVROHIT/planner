'use client';

import { WeekTaskItem } from './WeekTaskItem';
import { AddTaskDialog } from './AddTaskDialog';
import type { DayProgress, DayOfWeek, Task } from '@/types/domain';
import { cn } from '@/lib/utils';
import { isPastDate, isToday } from '@/lib/week/utils';
import { useState } from 'react';

interface DayColumnProps {
  day: DayProgress;
  dayOfWeek: DayOfWeek;
  tasks: Task[];
  weekStart: string;
  isEditable: boolean;
  onRemoveTask?: (taskId: string, dayOfWeek: DayOfWeek) => void;
}

/**
 * DayColumn component - renders a single day column in the week grid.
 * 
 * Displays:
 * - Date header with completion stats
 * - Task list
 * - Add task button (for future days)
 * - Closed indicator (for closed/past days)
 * 
 * Visual rules:
 * - Past/closed days: Frozen (grayed, no interactions)
 * - Future days: Editable (full color, interactive)
 * - Today: Highlighted
 */
export function DayColumn({
  day,
  dayOfWeek,
  tasks,
  weekStart,
  isEditable,
  onRemoveTask,
}: DayColumnProps) {
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const isPast = isPastDate(day.date);
  const isTodayDate = isToday(day.date);

  return (
    <div
      className={cn(
        'flex flex-col border-r border-border last:border-r-0',
        !isEditable && 'opacity-60'
      )}
    >
      {/* Date header */}
      <div
        className={cn(
          'p-3 border-b border-border bg-muted/30',
          isTodayDate && 'bg-primary/10 border-primary/20'
        )}
      >
        <div className="flex items-center justify-between mb-1">
          <div>
            <p className="text-sm font-semibold">{dayOfWeek.slice(0, 3)}</p>
            <p className="text-xs text-muted-foreground">
              {new Date(day.date).toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric',
              })}
            </p>
          </div>
          {day.closed && (
            <span className="text-xs text-muted-foreground bg-muted px-2 py-0.5 rounded">
              Closed
            </span>
          )}
        </div>
        {day.total > 0 && (
          <div className="flex items-center gap-1 text-xs text-muted-foreground">
            <span>{day.completed}</span>
            <span>/</span>
            <span>{day.total}</span>
          </div>
        )}
      </div>

      {/* Task list */}
      <div className="flex-1 p-2 space-y-2 min-h-[200px]">
        {tasks.map((task) => (
          <WeekTaskItem
            key={task.id}
            task={task}
            day={day}
            isEditable={isEditable}
            onRemove={onRemoveTask}
          />
        ))}
        {tasks.length === 0 && (
          <p className="text-xs text-muted-foreground text-center py-4">
            No tasks
          </p>
        )}
      </div>

      {/* Add task button */}
      {isEditable && !day.closed && (
        <div className="p-2 border-t border-border">
          <button
            onClick={() => setIsAddDialogOpen(true)}
            className="w-full px-3 py-2 text-xs font-medium rounded-md border border-dashed border-border hover:border-primary transition-colors text-muted-foreground hover:text-foreground"
          >
            + Add Task
          </button>
          <AddTaskDialog
            isOpen={isAddDialogOpen}
            onClose={() => setIsAddDialogOpen(false)}
            day={day.date}
            weekStart={weekStart}
          />
        </div>
      )}
    </div>
  );
}

export default DayColumn;
