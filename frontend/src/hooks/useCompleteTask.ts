'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import type { TodayDashboard } from '@/types/domain';
import { useToast } from '@/providers/ToastProvider';
import { getErrorMessage } from '@/lib/errors';

/**
 * Hook for completing a task in today's daily plan.
 * 
 * Performs optimistic update and invalidates today dashboard on success.
 * 
 * @param date ISO date string (YYYY-MM-DD) - defaults to today
 * @returns Mutation function and state
 * 
 * @example
 * const { completeTask, isLoading } = useCompleteTask();
 * await completeTask({ taskId: 'task-123', date: '2026-01-25' });
 */
export function useCompleteTask(date?: string) {
  const queryClient = useQueryClient();
  const { success, error: showToastError } = useToast();

  return useMutation({
    mutationFn: async ({ taskId, date: taskDate }: { taskId: string; date?: string }): Promise<void> => {
      const targetDate = taskDate || date || new Date().toISOString().split('T')[0];
      return api.post<void>(`/api/daily/${targetDate}/tasks/${taskId}/complete`);
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
            entries: previousDashboard.todayPlan.entries.map((entry) =>
              entry.taskId === taskId
                ? { ...entry, status: 'COMPLETED' }
                : entry
            ),
          },
          completionRatio: calculateOptimisticRatio(
            previousDashboard.todayPlan.entries,
            taskId,
            true
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
      success('Task marked as complete');
    },
  });
}

/**
 * Calculate optimistic completion ratio.
 * Helper function for optimistic updates - does NOT compute meaning,
 * just updates the ratio based on the optimistic state change.
 */
function calculateOptimisticRatio(
  entries: Array<{ taskId: string; status: 'PENDING' | 'COMPLETED' | 'MISSED' }>,
  taskId: string,
  completed: boolean
): number {
  if (entries.length === 0) return 0;

  const updatedEntries = entries.map((entry) =>
    entry.taskId === taskId ? { ...entry, status: completed ? 'COMPLETED' : 'PENDING' } : entry
  );

  const completedCount = updatedEntries.filter((t) => t.status === 'COMPLETED').length;
  return completedCount / updatedEntries.length;
}

export default useCompleteTask;
