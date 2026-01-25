'use client';

import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { Goal } from '@/types/domain';

/**
 * Hook for fetching all goals.
 * 
 * Returns a simple list of goals without snapshots or analytics.
 * Use this for dropdowns, selectors, or simple goal lists.
 * 
 * For the full Goals screen with progress/trends, use useGoalsDashboard instead.
 * 
 * @returns Array of Goal objects
 * 
 * @example
 * const { data: goals, isLoading } = useGoals();
 */
export function useGoals() {
    return useQuery({
        queryKey: ['goals'],
        queryFn: async (): Promise<Goal[]> => {
            return api.get<Goal[]>('/api/goals');
        },
        staleTime: 60000, // 1 minute
    });
}

export default useGoals;
