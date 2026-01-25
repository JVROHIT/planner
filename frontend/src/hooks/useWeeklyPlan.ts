'use client';

import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { WeeklyPlan } from '@/types/domain';

/**
 * Hook for fetching a weekly plan for a specific date.
 * 
 * The date is used to determine which week's plan to fetch.
 * 
 * @param date ISO date string (YYYY-MM-DD) - any date within the week
 * @returns WeeklyPlan with task grid for the week
 * 
 * @example
 * const { data, isLoading, error } = useWeeklyPlan('2026-01-25');
 */
export function useWeeklyPlan(date: string) {
  return useQuery({
    queryKey: ['weekly-plan', date],
    queryFn: async (): Promise<WeeklyPlan> => {
      return api.get<WeeklyPlan>(`/api/weekly-plan/${date}`);
    },
    refetchOnWindowFocus: true,
    staleTime: 30000, // 30 seconds
  });
}

export default useWeeklyPlan;
