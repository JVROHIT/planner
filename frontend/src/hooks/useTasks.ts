'use client';

import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { Task } from '@/types/domain';

/**
 * Hook for fetching all tasks for the authenticated user.
 * 
 * @returns Task[] for the current user
 * 
 * @example
 * const { data: tasks, isLoading } = useTasks();
 */
export function useTasks() {
  return useQuery({
    queryKey: ['tasks'],
    queryFn: async (): Promise<Task[]> => {
      return api.get<Task[]>('/api/tasks');
    },
    refetchOnWindowFocus: true,
    staleTime: 30000, // 30 seconds
  });
}

export default useTasks;
