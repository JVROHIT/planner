import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import { useToast } from '@/providers/ToastProvider';
import { getErrorMessage } from '@/lib/errors';
import type { KeyResult, CreateKeyResultRequest } from '@/types/domain';

/**
 * Hook for creating a new key result.
 */
export function useCreateKeyResult() {
    const queryClient = useQueryClient();
    const { success, error: showToastError } = useToast();

    return useMutation({
        mutationFn: async ({ goalId, request }: { goalId: string; request: CreateKeyResultRequest }): Promise<KeyResult> => {
            return api.post<KeyResult>(`/api/goals/${goalId}/key-results`, request);
        },
        onSuccess: (_, variables) => {
            // Invalidate key results for this goal
            queryClient.invalidateQueries({ queryKey: ['key-results', variables.goalId] });
            // Invalidate goals dashboard to refresh progress
            queryClient.invalidateQueries({ queryKey: ['goals-dashboard'] });
            queryClient.invalidateQueries({ queryKey: ['goals'] });
            success('Key result added');
        },
        onError: (error: ApiError) => {
            const errorInfo = getErrorMessage(error.status, error.errorCode, error.message);
            showToastError(errorInfo.message, errorInfo.title);
        },
    });
}

export default useCreateKeyResult;
