'use client';

import { DayColumn } from './DayColumn';
import { useTasks, useUpdateWeeklyPlan, useWeeklyPlan } from '@/hooks';
import type { DayProgress, WeeklyPlan, DayOfWeek, Task } from '@/types/domain';
import { isPastDate } from '@/lib/week/utils';
import { Skeleton } from '@/components/ui/Skeleton';

interface WeeklyGridProps {
  weekProgress: DayProgress[] | undefined;
  weeklyPlan: WeeklyPlan | undefined;
  weekStart: string;
  isLoading: boolean;
}

const DAYS_OF_WEEK: DayOfWeek[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
];

/**
 * WeeklyGrid component - renders a 7-day grid for the week view.
 * 
 * Displays 7 DayColumns (Mon-Sun) with tasks from the weekly plan.
 * Handles drag-and-drop between days (future implementation).
 * 
 * Visual rules:
 * - Past/closed days: Frozen (grayed, no drag)
 * - Future days: Editable (full color, draggable)
 * - Today: Highlighted
 */
export function WeeklyGrid({
  weekProgress,
  weeklyPlan: weeklyPlanProp,
  weekStart,
  isLoading,
}: WeeklyGridProps) {
  const { data: allTasks, isLoading: isLoadingTasks } = useTasks();
  const { data: weeklyPlanData } = useWeeklyPlan(weekStart);
  const { mutateAsync: updateWeeklyPlan } = useUpdateWeeklyPlan();

  // Use prop if available, otherwise use query data
  const weeklyPlan = weeklyPlanProp || weeklyPlanData;

  const handleRemoveTask = async (taskId: string, dayOfWeek: DayOfWeek) => {
    if (!weeklyPlan) return;

    const currentTaskIds = weeklyPlan.taskGrid[dayOfWeek] || [];
    const updatedTaskIds = currentTaskIds.filter((id) => id !== taskId);
    const updatedTaskGrid = {
      ...weeklyPlan.taskGrid,
      [dayOfWeek]: updatedTaskIds,
    };

    await updateWeeklyPlan({
      weekNumber: weeklyPlan.weekNumber,
      year: weeklyPlan.year,
      taskGrid: updatedTaskGrid,
    });
  };

  if (isLoading || isLoadingTasks) {
    return (
      <div className="grid grid-cols-7 border border-border rounded-lg overflow-hidden">
        {DAYS_OF_WEEK.map((day) => (
          <Skeleton key={day} className="h-[400px] rounded-none border-r last:border-r-0" />
        ))}
      </div>
    );
  }

  if (!weekProgress || !weeklyPlan) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground text-sm">No week data available</p>
      </div>
    );
  }

  // Create a map of taskId -> Task for quick lookup
  const taskByIdMap = new Map<string, Task>();
  (allTasks || []).forEach((task) => {
    taskByIdMap.set(task.id, task);
  });

  // Create a map of date -> DayProgress
  const progressMap = new Map<string, DayProgress>();
  weekProgress.forEach((progress) => {
    progressMap.set(progress.date, progress);
  });

  // Generate dates for the week (Monday to Sunday)
  const weekDates: Array<{ date: string; dayOfWeek: DayOfWeek }> = [];
  const startDate = new Date(weekStart);
  DAYS_OF_WEEK.forEach((dayOfWeek, index) => {
    const date = new Date(startDate);
    date.setDate(startDate.getDate() + index);
    weekDates.push({
      date: date.toISOString().split('T')[0],
      dayOfWeek,
    });
  });

  return (
    <div className="grid grid-cols-7 gap-0 border border-border rounded-lg overflow-hidden">
      {weekDates.map(({ date, dayOfWeek }) => {
        const progress = progressMap.get(date) || {
          date,
          dayOfWeek,
          completed: 0,
          total: 0,
          closed: isPastDate(date) || false,
        };
        const taskIds = weeklyPlan.taskGrid[dayOfWeek] || [];
        const tasks = taskIds
          .map((taskId) => taskByIdMap.get(taskId))
          .filter((task): task is Task => task !== undefined);
        const isEditable = !progress.closed && !isPastDate(date);

        return (
          <DayColumn
            key={date}
            day={progress}
            dayOfWeek={dayOfWeek}
            tasks={tasks}
            weekStart={weekStart}
            isEditable={isEditable}
            onRemoveTask={handleRemoveTask}
          />
        );
      })}
    </div>
  );
}

export default WeeklyGrid;
