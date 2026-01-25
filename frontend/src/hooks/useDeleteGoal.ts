'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';

/**
 * Hook for deleting a goal.
 * 
 * Invalidates goals queries on success to refetch fresh data.
 * 
 * @returns Mutation function and state
 * 
 * @example
 * const { mutate: deleteGoal, isPending } = useDeleteGoal();
 * deleteGoal('goal-123');
 */
export function useDeleteGoal() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (goalId: string): Promise<void> => {
            return api.delete(`/api/goals/${goalId}`);
        },
        onSuccess: () => {
            // Invalidate both goals and goals-dashboard queries
            queryClient.invalidateQueries({ queryKey: ['goals'] });
            queryClient.invalidateQueries({ queryKey: ['goals-dashboard'] });
        },
        onError: (error: ApiError) => {
            console.error('Failed to delete goal:', error.message);
        },
    });
}

export default useDeleteGoal;
