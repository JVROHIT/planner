import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import { useToast } from '@/providers/ToastProvider';
import { getErrorMessage } from '@/lib/errors';
import type { Goal, CreateGoalRequest } from '@/types/domain';

/**
 * Hook for creating a new goal.
 */
export function useCreateGoal() {
    const queryClient = useQueryClient();
    const { success, error: showToastError } = useToast();

    return useMutation({
        mutationFn: async (request: CreateGoalRequest): Promise<Goal> => {
            return api.post<Goal>('/api/goals', request);
        },
        onSuccess: () => {
            // Invalidate both goals and goals-dashboard queries
            queryClient.invalidateQueries({ queryKey: ['goals'] });
            queryClient.invalidateQueries({ queryKey: ['goals-dashboard'] });
            success('Goal created successfully');
        },
        onError: (error: ApiError) => {
            const errorInfo = getErrorMessage(error.status, error.errorCode, error.message);
            showToastError(errorInfo.message, errorInfo.title);
        },
    });
}

export default useCreateGoal;
