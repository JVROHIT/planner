'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import { useToast } from '@/providers/ToastProvider';
import { getErrorMessage } from '@/lib/errors';

/**
 * Hook for deleting a key result.
 * 
 * Invalidates key results and goals queries on success.
 * 
 * @returns Mutation function and state
 * 
 * @example
 * const { mutate: deleteKeyResult, isPending } = useDeleteKeyResult();
 * deleteKeyResult({ goalId: 'goal-123', keyResultId: 'kr-456' });
 */
export function useDeleteKeyResult() {
    const queryClient = useQueryClient();
    const { success, error: showToastError } = useToast();

    return useMutation({
        mutationFn: async ({ keyResultId }: { goalId: string; keyResultId: string }): Promise<void> => {
            return api.delete(`/api/goals/key-results/${keyResultId}`);
        },
        onSuccess: (_, variables) => {
            // Invalidate key results for this goal
            queryClient.invalidateQueries({ queryKey: ['key-results', variables.goalId] });
            // Invalidate goals dashboard to refresh progress
            queryClient.invalidateQueries({ queryKey: ['goals-dashboard'] });
            queryClient.invalidateQueries({ queryKey: ['goals'] });
            success('Key result deleted');
        },
        onError: (error: ApiError) => {
            const errorInfo = getErrorMessage(error.status, error.errorCode, error.message);
            showToastError(errorInfo.message, errorInfo.title);
        },
    });
}

export default useDeleteKeyResult;
