'use client';

import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { AuditEvent } from '@/types/domain';

/**
 * Hook for fetching recent audit events.
 * 
 * Truth mode - what actually happened.
 * Read-only, no mutations.
 * 
 * @param limit Number of events to fetch (default: 20)
 * @returns Array of recent AuditEvents
 */
export function useRecentHistory(limit: number = 20) {
    return useQuery({
        queryKey: ['recent-history', limit],
        queryFn: async (): Promise<AuditEvent[]> => {
            return api.get<AuditEvent[]>(`/api/history/recent?limit=${limit}`);
        },
        staleTime: 60000, // 1 minute
    });
}

export default useRecentHistory;
