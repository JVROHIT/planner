'use client';

import { useState } from 'react';
import * as Dialog from '@radix-ui/react-dialog';
import { useCreateTask, useUpdateWeeklyPlan, useWeeklyPlan } from '@/hooks';
import { getDayOfWeekFromDate } from '@/lib/week/utils';

interface AddTaskDialogProps {
  isOpen: boolean;
  onClose: () => void;
  day: string; // ISO date (YYYY-MM-DD)
  weekStart: string;
}

/**
 * AddTaskDialog component - dialog for adding a new task to a day.
 * Uses Radix UI for accessibility.
 */
export function AddTaskDialog({ isOpen, onClose, day, weekStart }: AddTaskDialogProps) {
  const [description, setDescription] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { mutateAsync: createTask } = useCreateTask();
  const { mutateAsync: updateWeeklyPlan } = useUpdateWeeklyPlan();
  const { data: weeklyPlan } = useWeeklyPlan(weekStart);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!description.trim() || !weeklyPlan) return;

    setIsSubmitting(true);
    try {
      // Create the task
      const newTask = await createTask({ description: description.trim() });

      // Add task to the weekly plan for this day
      const dayOfWeek = getDayOfWeekFromDate(day);
      const currentTaskIds = weeklyPlan.taskGrid[dayOfWeek] || [];
      const updatedTaskGrid = {
        ...weeklyPlan.taskGrid,
        [dayOfWeek]: [...currentTaskIds, newTask.id],
      };

      await updateWeeklyPlan({
        weekNumber: weeklyPlan.weekNumber,
        year: weeklyPlan.year,
        taskGrid: updatedTaskGrid,
      });

      setDescription('');
      onClose();
    } catch (error) {
      // Error handled by hooks
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Dialog.Root open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 bg-black/50 z-50 animate-in fade-in" />
        <Dialog.Content
          className="fixed left-[50%] top-[50%] translate-x-[-50%] translate-y-[-50%] bg-card border rounded-lg p-6 max-w-md w-full mx-4 shadow-xl z-50 animate-in zoom-in-95"
          aria-describedby="add-task-description"
        >
          <Dialog.Title className="text-xl font-semibold mb-4">Add Task</Dialog.Title>
          <div id="add-task-description" className="sr-only">Form to add a new task to your plan for {day}</div>

          <form onSubmit={handleSubmit}>
            <div className="space-y-4">
              {/* Title */}
              <div>
                <label htmlFor="task-description" className="block text-sm font-medium mb-2">
                  Task Description <span className="text-destructive">*</span>
                </label>
                <input
                  id="task-description"
                  type="text"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="e.g., Attend team standup"
                  className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-background"
                  autoFocus
                  required
                />
              </div>

              {/* Actions */}
              <div className="flex gap-3 justify-end pt-2">
                <Dialog.Close asChild>
                  <button
                    type="button"
                    className="px-4 py-2 border rounded-lg hover:bg-muted transition-colors"
                    disabled={isSubmitting}
                  >
                    Cancel
                  </button>
                </Dialog.Close>
                <button
                  type="submit"
                  className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  disabled={isSubmitting || !description.trim()}
                >
                  {isSubmitting ? 'Adding...' : 'Add Task'}
                </button>
              </div>
            </div>
          </form>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}


export default AddTaskDialog;
