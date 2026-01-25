'use client';

import { useState } from 'react';
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
 * 
 * Allows user to:
 * - Enter task description
 * - Optionally link to goal/key result (future)
 * - Create task and add to the specified day
 * 
 * Enforces: no modification of closed days.
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
      console.error('Failed to create task:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-background border border-border rounded-lg p-6 w-full max-w-md">
        <h2 className="text-lg font-semibold mb-4">Add Task</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="description" className="block text-sm font-medium mb-1">
              Task Description
            </label>
            <input
              id="description"
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Enter task description..."
              className="w-full px-3 py-2 border border-border rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
              required
              autoFocus
            />
          </div>
          <div className="flex gap-2 justify-end">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium rounded-md border border-border hover:bg-muted transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!description.trim() || isSubmitting}
              className="px-4 py-2 text-sm font-medium rounded-md bg-primary text-primary-foreground hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {isSubmitting ? 'Adding...' : 'Add Task'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AddTaskDialog;
