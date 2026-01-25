'use client';

import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { TodayDashboard } from '@/types/domain';

/**
 * Hook for fetching today's dashboard data.
 * 
 * This is the SINGLE authoritative query for the Today screen.
 * Never read from Task directly - all data comes from DailyPlan.
 * 
 * @returns TodayDashboard with today's plan, completion ratio, streak, and goal summaries
 * 
 * @example
 * const { data, isLoading, error } = useTodayDashboard();
 */
export function useTodayDashboard() {
  return useQuery({
    queryKey: ['today-dashboard'],
    queryFn: async (): Promise<TodayDashboard> => {
      return api.get<TodayDashboard>('/api/dashboard/today');
    },
    refetchOnWindowFocus: true,
    staleTime: 30000, // 30 seconds
  });
}

export default useTodayDashboard;
