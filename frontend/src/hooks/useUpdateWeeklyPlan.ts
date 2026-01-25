'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import type { WeeklyPlan } from '@/types/domain';

/**
 * Hook for updating a weekly plan.
 * 
 * Updates the task grid for the week. The backend handles both create and update.
 * Invalidates week-related queries on success.
 * 
 * @returns Mutation function and state
 * 
 * @example
 * const { mutateAsync: updatePlan, isLoading } = useUpdateWeeklyPlan();
 * await updatePlan({
 *   weekNumber: 4,
 *   year: 2026,
 *   taskGrid: { MONDAY: ['task-1'], TUESDAY: [], ... }
 * });
 */
export function useUpdateWeeklyPlan() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (request: {
      weekNumber: number;
      year: number;
      taskGrid: Record<string, string[]>;
    }): Promise<WeeklyPlan> => {
      return api.post<WeeklyPlan>('/api/weekly-plan', request);
    },
    onError: () => {
      // Error handled by component
    },
    onSuccess: (data) => {
      // Invalidate all week-related queries
      queryClient.invalidateQueries({ queryKey: ['weekly-plan'] });
      queryClient.invalidateQueries({ queryKey: ['week-dashboard'] });
      
      // Also invalidate for the specific week
      const weekStartDate = data.weekStartDate;
      queryClient.invalidateQueries({ queryKey: ['weekly-plan', weekStartDate] });
      queryClient.invalidateQueries({ queryKey: ['week-dashboard', weekStartDate] });
    },
  });
}

export default useUpdateWeeklyPlan;
