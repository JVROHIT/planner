'use client';

import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { DailyPlan } from '@/types/domain';

/**
 * Hook for fetching a previous day's daily plan.
 * 
 * Truth mode - what actually happened.
 * Read-only, no mutations.
 * 
 * @param date ISO date string (YYYY-MM-DD)
 * @returns DailyPlan for the specified date
 */
export function useHistoryDay(date: string) {
    return useQuery({
        queryKey: ['history-day', date],
        queryFn: async (): Promise<DailyPlan> => {
            return api.get<DailyPlan>(`/api/daily/${date}`);
        },
        enabled: !!date,
        staleTime: 300000, // 5 minutes, history is mostly static
    });
}

export default useHistoryDay;
