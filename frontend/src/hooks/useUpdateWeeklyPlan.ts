import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import type { WeeklyPlan } from '@/types/domain';
import { useToast } from '@/providers/ToastProvider';
import { getErrorMessage } from '@/lib/errors';

/**
 * Hook for updating a weekly plan.
 */
export function useUpdateWeeklyPlan() {
  const queryClient = useQueryClient();
  const { success, error: showToastError } = useToast();

  return useMutation({
    mutationFn: async (request: {
      weekNumber: number;
      year: number;
      taskGrid: Record<string, string[]>;
    }): Promise<WeeklyPlan> => {
      return api.post<WeeklyPlan>('/api/weekly-plan', request);
    },
    onError: (error: ApiError) => {
      const errorInfo = getErrorMessage(error.status, error.errorCode, error.message);
      showToastError(errorInfo.message, errorInfo.title);
    },
    onSuccess: (data) => {
      // Invalidate all week-related queries
      queryClient.invalidateQueries({ queryKey: ['weekly-plan'] });
      queryClient.invalidateQueries({ queryKey: ['week-dashboard'] });

      // Also invalidate for the specific week
      const weekStartDate = data.weekStartDate;
      queryClient.invalidateQueries({ queryKey: ['weekly-plan', weekStartDate] });
      queryClient.invalidateQueries({ queryKey: ['week-dashboard', weekStartDate] });

      success('Weekly plan updated');
    },
  });
}

export default useUpdateWeeklyPlan;
