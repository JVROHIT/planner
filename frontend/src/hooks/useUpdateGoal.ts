import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import { useToast } from '@/providers/ToastProvider';
import { getErrorMessage } from '@/lib/errors';
import type { Goal, UpdateGoalRequest } from '@/types/domain';

/**
 * Hook for updating an existing goal.
 */
export function useUpdateGoal() {
    const queryClient = useQueryClient();
    const { success, error: showToastError } = useToast();

    return useMutation({
        mutationFn: async ({ id, updates }: { id: string; updates: UpdateGoalRequest }): Promise<Goal> => {
            return api.put<Goal>(`/api/goals/${id}`, updates);
        },
        onSuccess: () => {
            // Invalidate both goals and goals-dashboard queries
            queryClient.invalidateQueries({ queryKey: ['goals'] });
            queryClient.invalidateQueries({ queryKey: ['goals-dashboard'] });
            success('Goal updated successfully');
        },
        onError: (error: ApiError) => {
            const errorInfo = getErrorMessage(error.status, error.errorCode, error.message);
            showToastError(errorInfo.message, errorInfo.title);
        },
    });
}

export default useUpdateGoal;
