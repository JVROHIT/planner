import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import type { KeyResult } from '@/types/domain';
import { useToast } from '@/providers/ToastProvider';
import { getErrorMessage } from '@/lib/errors';

/**
 * Hook for completing a milestone key result.
 * 
 * Only applicable to MILESTONE type key results.
 * Invalidates key results and goals queries on success.
 * 
 * @returns Mutation function and state
 */
export function useCompleteMilestone() {
    const queryClient = useQueryClient();
    const { success, error: showToastError } = useToast();

    return useMutation({
        mutationFn: async ({ keyResultId }: { goalId: string; keyResultId: string }): Promise<KeyResult> => {
            return api.post<KeyResult>(`/api/goals/key-results/${keyResultId}/complete`);
        },
        onSuccess: (_, variables) => {
            // Invalidate key results for this goal
            queryClient.invalidateQueries({ queryKey: ['key-results', variables.goalId] });
            // Invalidate goals dashboard to refresh progress
            queryClient.invalidateQueries({ queryKey: ['goals-dashboard'] });
            queryClient.invalidateQueries({ queryKey: ['goals'] });
            success('Milestone completed');
        },
        onError: (error: ApiError) => {
            const errorInfo = getErrorMessage(error.status, error.errorCode, error.message);
            showToastError(errorInfo.message, errorInfo.title);
        },
    });
}

export default useCompleteMilestone;
