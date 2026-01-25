'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import type { TodayDashboard } from '@/types/domain';
import { useToast } from '@/providers/ToastProvider';
import { getErrorMessage } from '@/lib/errors';

/**
 * Hook for marking a task as missed in today's daily plan.
 * 
 * Performs optimistic update and invalidates today dashboard on success.
 * 
 * @param date ISO date string (YYYY-MM-DD) - defaults to today
 * @returns Mutation function and state
 * 
 * @example
 * const { missTask, isLoading } = useMissTask();
 * await missTask({ taskId: 'task-123', date: '2026-01-25' });
 */
export function useMissTask(date?: string) {
  const queryClient = useQueryClient();
  const { success, error: showToastError } = useToast();

  return useMutation({
    mutationFn: async ({ taskId, date: taskDate }: { taskId: string; date?: string }): Promise<void> => {
      const targetDate = taskDate || date || new Date().toISOString().split('T')[0];
      return api.post<void>(`/api/daily/${targetDate}/tasks/${taskId}/miss`);
    },
    onMutate: async ({ taskId }) => {
      const queryKey = ['today-dashboard'];

      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey });

      // Snapshot previous value for rollback
      const previousDashboard = queryClient.getQueryData<TodayDashboard>(queryKey);

      // Optimistically update
      if (previousDashboard?.todayPlan) {
        const optimisticDashboard: TodayDashboard = {
          ...previousDashboard,
          todayPlan: {
            ...previousDashboard.todayPlan,
            tasks: previousDashboard.todayPlan.tasks.map((task) =>
              task.taskId === taskId
                ? { ...task, missed: true, completed: false }
                : task
            ),
          },
          completionRatio: calculateOptimisticRatio(
            previousDashboard.todayPlan.tasks,
            taskId,
            false
          ),
        };
        queryClient.setQueryData(queryKey, optimisticDashboard);
      }

      return { previousDashboard };
    },
    onError: (error: ApiError, variables, context) => {
      // Rollback on error
      if (context?.previousDashboard) {
        queryClient.setQueryData(['today-dashboard'], context.previousDashboard);
      }

      const errorInfo = getErrorMessage(error.status, error.errorCode, error.message);
      showToastError(errorInfo.message, errorInfo.title);
    },
    onSuccess: () => {
      // Invalidate to refetch fresh data
      queryClient.invalidateQueries({ queryKey: ['today-dashboard'] });
      success('Task marked as missed');
    },
  });
}

/**
 * Calculate optimistic completion ratio.
 * Helper function for optimistic updates - does NOT compute meaning,
 * just updates the ratio based on the optimistic state change.
 */
function calculateOptimisticRatio(
  tasks: Array<{ taskId: string; completed: boolean; missed: boolean }>,
  taskId: string,
  completed: boolean
): number {
  if (tasks.length === 0) return 0;

  const updatedTasks = tasks.map((task) =>
    task.taskId === taskId ? { ...task, completed, missed: !completed } : task
  );

  const completedCount = updatedTasks.filter((t) => t.completed).length;
  return completedCount / updatedTasks.length;
}

export default useMissTask;
