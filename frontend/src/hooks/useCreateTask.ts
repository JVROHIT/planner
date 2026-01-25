'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import type { Task, CreateTaskRequest } from '@/types/domain';

/**
 * Hook for creating a new task.
 * 
 * Creates a task and invalidates task-related queries on success.
 * 
 * @returns Mutation function and state
 * 
 * @example
 * const { mutateAsync: createTask, isLoading } = useCreateTask();
 * const task = await createTask({
 *   description: 'New task',
 *   goalId: 'goal-123',
 *   keyResultId: 'kr-456'
 * });
 */
export function useCreateTask() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (request: CreateTaskRequest): Promise<Task> => {
      return api.post<Task>('/api/tasks', request);
    },
    onError: (error: ApiError) => {
      // Error handled by component
    },
    onSuccess: () => {
      // Invalidate task queries
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      // Also invalidate weekly plans since tasks are part of the grid
      queryClient.invalidateQueries({ queryKey: ['weekly-plan'] });
    },
  });
}

export default useCreateTask;
