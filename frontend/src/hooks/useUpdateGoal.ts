'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import type { Goal, UpdateGoalRequest } from '@/types/domain';

/**
 * Hook for updating an existing goal.
 * 
 * Invalidates goals queries on success to refetch fresh data.
 * 
 * @returns Mutation function and state
 * 
 * @example
 * const { mutate: updateGoal, isPending } = useUpdateGoal();
 * updateGoal({ id: 'goal-123', updates: { title: 'Updated title' } });
 */
export function useUpdateGoal() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, updates }: { id: string; updates: UpdateGoalRequest }): Promise<Goal> => {
            return api.put<Goal>(`/api/goals/${id}`, updates);
        },
        onSuccess: () => {
            // Invalidate both goals and goals-dashboard queries
            queryClient.invalidateQueries({ queryKey: ['goals'] });
            queryClient.invalidateQueries({ queryKey: ['goals-dashboard'] });
        },
        onError: (error: ApiError) => {
            console.error('Failed to update goal:', error.message);
        },
    });
}

export default useUpdateGoal;
