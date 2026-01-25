'use client';

import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { GoalsDashboard } from '@/types/domain';

/**
 * Hook for fetching goals dashboard data.
 * 
 * This is the SINGLE authoritative query for the Goals screen.
 * Returns goals with snapshots, trends, and progress status.
 * 
 * Philosophy: Never recompute progress client-side - render what backend asserts.
 * 
 * @returns GoalsDashboard with goals, key results, snapshots, and derived meaning
 * 
 * @example
 * const { data, isLoading, error } = useGoalsDashboard();
 */
export function useGoalsDashboard() {
  return useQuery({
    queryKey: ['goals-dashboard'],
    queryFn: async (): Promise<GoalsDashboard> => {
      return api.get<GoalsDashboard>('/api/dashboard/goals');
    },
    refetchOnWindowFocus: true,
    staleTime: 30000, // 30 seconds
  });
}

export default useGoalsDashboard;
