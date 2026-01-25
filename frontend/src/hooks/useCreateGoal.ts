'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import type { Goal, CreateGoalRequest } from '@/types/domain';

/**
 * Hook for creating a new goal.
 * 
 * Invalidates goals queries on success to refetch fresh data.
 * 
 * @returns Mutation function and state
 * 
 * @example
 * const { mutate: createGoal, isPending } = useCreateGoal();
 * createGoal({ title: 'Read 12 books', description: 'One per month' });
 */
export function useCreateGoal() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (request: CreateGoalRequest): Promise<Goal> => {
            return api.post<Goal>('/api/goals', request);
        },
        onSuccess: () => {
            // Invalidate both goals and goals-dashboard queries
            queryClient.invalidateQueries({ queryKey: ['goals'] });
            queryClient.invalidateQueries({ queryKey: ['goals-dashboard'] });
        },
        onError: (error: ApiError) => {
            console.error('Failed to create goal:', error.message);
        },
    });
}

export default useCreateGoal;
