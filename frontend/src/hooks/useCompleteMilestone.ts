'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import type { KeyResult } from '@/types/domain';

/**
 * Hook for completing a milestone key result.
 * 
 * Only applicable to MILESTONE type key results.
 * Invalidates key results and goals queries on success.
 * 
 * @returns Mutation function and state
 * 
 * @example
 * const { mutate: completeMilestone, isPending } = useCompleteMilestone();
 * completeMilestone({ goalId: 'goal-123', keyResultId: 'kr-456' });
 */
export function useCompleteMilestone() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ goalId, keyResultId }: { goalId: string; keyResultId: string }): Promise<KeyResult> => {
            return api.post<KeyResult>(`/api/goals/key-results/${keyResultId}/complete`);
        },
        onSuccess: (_, variables) => {
            // Invalidate key results for this goal
            queryClient.invalidateQueries({ queryKey: ['key-results', variables.goalId] });
            // Invalidate goals dashboard to refresh progress
            queryClient.invalidateQueries({ queryKey: ['goals-dashboard'] });
            queryClient.invalidateQueries({ queryKey: ['goals'] });
        },
        onError: (error: ApiError) => {
            console.error('Failed to complete milestone:', error.message);
        },
    });
}

export default useCompleteMilestone;
