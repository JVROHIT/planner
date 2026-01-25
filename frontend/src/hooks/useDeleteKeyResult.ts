'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';

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

    return useMutation({
        mutationFn: async ({ goalId, keyResultId }: { goalId: string; keyResultId: string }): Promise<void> => {
            return api.delete(`/api/goals/key-results/${keyResultId}`);
        },
        onSuccess: (_, variables) => {
            // Invalidate key results for this goal
            queryClient.invalidateQueries({ queryKey: ['key-results', variables.goalId] });
            // Invalidate goals dashboard to refresh progress
            queryClient.invalidateQueries({ queryKey: ['goals-dashboard'] });
            queryClient.invalidateQueries({ queryKey: ['goals'] });
        },
        onError: (error: ApiError) => {
            console.error('Failed to delete key result:', error.message);
        },
    });
}

export default useDeleteKeyResult;
