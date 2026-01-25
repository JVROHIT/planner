import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import type { Task, CreateTaskRequest } from '@/types/domain';
import { useToast } from '@/providers/ToastProvider';
import { getErrorMessage } from '@/lib/errors';

/**
 * Hook for creating a new task.
 * 
 * Creates a task and invalidates task-related queries on success.
 * 
 * @returns Mutation function and state
 */
export function useCreateTask() {
  const queryClient = useQueryClient();
  const { success, error: showToastError } = useToast();

  return useMutation({
    mutationFn: async (request: CreateTaskRequest): Promise<Task> => {
      return api.post<Task>('/api/tasks', request);
    },
    onError: (error: ApiError) => {
      const errorInfo = getErrorMessage(error.status, error.errorCode, error.message);
      showToastError(errorInfo.message, errorInfo.title);
    },
    onSuccess: () => {
      // Invalidate task queries
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      // Also invalidate weekly plans since tasks are part of the grid
      queryClient.invalidateQueries({ queryKey: ['weekly-plan'] });
      success('Task created successfully');
    },
  });
}

export default useCreateTask;
