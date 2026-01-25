'use client';

import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { KeyResult } from '@/types/domain';

/**
 * Hook for fetching key results for a specific goal.
 * 
 * @param goalId The goal ID to fetch key results for
 * @returns Array of KeyResult objects
 * 
 * @example
 * const { data: keyResults, isLoading } = useKeyResults('goal-123');
 */
export function useKeyResults(goalId: string) {
    return useQuery({
        queryKey: ['key-results', goalId],
        queryFn: async (): Promise<KeyResult[]> => {
            return api.get<KeyResult[]>(`/api/goals/${goalId}/key-results`);
        },
        enabled: !!goalId,
        staleTime: 60000, // 1 minute
    });
}

export default useKeyResults;
