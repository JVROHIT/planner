import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import type { KeyResult, UpdateKeyResultRequest } from '@/types/domain';
import { useToast } from '@/providers/ToastProvider';
import { getErrorMessage } from '@/lib/errors';

/**
 * Hook for updating an existing key result.
 * 
 * Invalidates key results and goals queries on success.
 * 
 * @returns Mutation function and state
 */
export function useUpdateKeyResult() {
    const queryClient = useQueryClient();
    const { success, error: showToastError } = useToast();

    return useMutation({
        mutationFn: async ({
            keyResultId,
            updates
        }: {
            goalId: string;
            keyResultId: string;
            updates: UpdateKeyResultRequest
        }): Promise<KeyResult> => {
            return api.put<KeyResult>(`/api/goals/key-results/${keyResultId}`, updates);
        },
        onSuccess: (_, variables) => {
            // Invalidate key results for this goal
            queryClient.invalidateQueries({ queryKey: ['key-results', variables.goalId] });
            // Invalidate goals dashboard to refresh progress
            queryClient.invalidateQueries({ queryKey: ['goals-dashboard'] });
            queryClient.invalidateQueries({ queryKey: ['goals'] });
            success('Key result updated');
        },
        onError: (error: ApiError) => {
            const errorInfo = getErrorMessage(error.status, error.errorCode, error.message);
            showToastError(errorInfo.message, errorInfo.title);
        },
    });
}

export default useUpdateKeyResult;
