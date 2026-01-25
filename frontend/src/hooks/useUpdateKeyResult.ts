'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import type { KeyResult, UpdateKeyResultRequest } from '@/types/domain';

/**
 * Hook for updating an existing key result.
 * 
 * Invalidates key results and goals queries on success.
 * 
 * @returns Mutation function and state
 * 
 * @example
 * const { mutate: updateKeyResult, isPending } = useUpdateKeyResult();
 * updateKeyResult({ goalId: 'goal-123', keyResultId: 'kr-456', updates: { currentValue: 5 } });
 */
export function useUpdateKeyResult() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({
            goalId,
            keyResultId,
            updates
        }: {
            goalId: string;
            keyResultId: string;
            updates: UpdateKeyResultRequest
        }): Promise<KeyResult> => {
            return api.put<KeyResult>(`/api/goals/key-results/${keyResultId}`, updates);
        },
        onSuccess: (_, variables) => {
            // Invalidate key results for this goal
            queryClient.invalidateQueries({ queryKey: ['key-results', variables.goalId] });
            // Invalidate goals dashboard to refresh progress
            queryClient.invalidateQueries({ queryKey: ['goals-dashboard'] });
            queryClient.invalidateQueries({ queryKey: ['goals'] });
        },
        onError: (error: ApiError) => {
            console.error('Failed to update key result:', error.message);
        },
    });
}

export default useUpdateKeyResult;
