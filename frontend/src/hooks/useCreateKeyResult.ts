'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import type { KeyResult, CreateKeyResultRequest } from '@/types/domain';

/**
 * Hook for creating a new key result.
 * 
 * Invalidates key results and goals queries on success.
 * 
 * @returns Mutation function and state
 * 
 * @example
 * const { mutate: createKeyResult, isPending } = useCreateKeyResult();
 * createKeyResult({ goalId: 'goal-123', request: { title: 'Read 12 books', type: 'ACCUMULATIVE', targetValue: 12 } });
 */
export function useCreateKeyResult() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ goalId, request }: { goalId: string; request: CreateKeyResultRequest }): Promise<KeyResult> => {
            return api.post<KeyResult>(`/api/goals/${goalId}/key-results`, request);
        },
        onSuccess: (_, variables) => {
            // Invalidate key results for this goal
            queryClient.invalidateQueries({ queryKey: ['key-results', variables.goalId] });
            // Invalidate goals dashboard to refresh progress
            queryClient.invalidateQueries({ queryKey: ['goals-dashboard'] });
            queryClient.invalidateQueries({ queryKey: ['goals'] });
        },
        onError: (error: ApiError) => {
            console.error('Failed to create key result:', error.message);
        },
    });
}

export default useCreateKeyResult;
