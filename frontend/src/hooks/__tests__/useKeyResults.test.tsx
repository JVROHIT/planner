import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import {
    useKeyResults,
    useCreateKeyResult,
    useUpdateKeyResult,
    useDeleteKeyResult,
    useCompleteMilestone,
} from '../';
import { api } from '@/lib/api';
import type { KeyResult } from '@/types/domain';
import { ToastProvider } from '@/providers/ToastProvider';

// Mock API
vi.mock('@/lib/api', () => ({
    api: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
    },
    ApiError: class ApiError extends Error {
        status: number;
        errorCode: string | null;
        constructor(message: string, status: number, errorCode: string | null = null) {
            super(message);
            this.status = status;
            this.errorCode = errorCode;
        }
    },
}));

// Create wrapper with QueryClient and ToastProvider
const createWrapper = () => {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: { retry: false },
            mutations: { retry: false },
        },
    });
    const Wrapper = ({ children }: { children: ReactNode }) => (
        <QueryClientProvider client={queryClient}>
            <ToastProvider>
                {children}
            </ToastProvider>
        </QueryClientProvider>
    );
    Wrapper.displayName = 'QueryClientWrapper';
    return Wrapper;
};

describe('useKeyResults', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('fetches from /api/goals/{goalId}/key-results', async () => {
        const mockKeyResults: KeyResult[] = [];
        vi.mocked(api.get).mockResolvedValueOnce(mockKeyResults);

        const { result } = renderHook(() => useKeyResults('goal-123'), { wrapper: createWrapper() });

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        expect(api.get).toHaveBeenCalledWith('/api/goals/goal-123/key-results');
    });

    it('returns KeyResult[] shape', async () => {
        const mockKeyResults: KeyResult[] = [
            {
                id: 'kr-1',
                goalId: 'goal-123',
                title: 'Read 12 books',
                type: 'ACCUMULATIVE',
                targetValue: 12,
                currentValue: 3,
                completed: false,
                createdAt: '2026-01-01T00:00:00Z',
                updatedAt: '2026-01-25T00:00:00Z',
            },
        ];
        vi.mocked(api.get).mockResolvedValueOnce(mockKeyResults);

        const { result } = renderHook(() => useKeyResults('goal-123'), { wrapper: createWrapper() });

        await waitFor(() => {
            expect(result.current.data).toEqual(mockKeyResults);
        });

        expect(result.current.data).toHaveLength(1);
        expect(result.current.data?.[0].type).toBe('ACCUMULATIVE');
    });
});

describe('useCreateKeyResult', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('calls POST /api/goals/{goalId}/key-results', async () => {
        const mockKeyResult: KeyResult = {
            id: 'kr-1',
            goalId: 'goal-123',
            title: 'New KR',
            type: 'MILESTONE',
            targetValue: 1,
            currentValue: 0,
            completed: false,
            createdAt: '2026-01-25T00:00:00Z',
            updatedAt: '2026-01-25T00:00:00Z',
        };
        vi.mocked(api.post).mockResolvedValueOnce(mockKeyResult);

        const { result } = renderHook(() => useCreateKeyResult(), { wrapper: createWrapper() });

        result.current.mutate({
            goalId: 'goal-123',
            request: { title: 'New KR', type: 'MILESTONE', targetValue: 1 },
        });

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        expect(api.post).toHaveBeenCalledWith('/api/goals/goal-123/key-results', {
            title: 'New KR',
            type: 'MILESTONE',
            targetValue: 1,
        });
    });
});

describe('useUpdateKeyResult', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('calls PUT /api/goals/key-results/{id}', async () => {
        const mockKeyResult: KeyResult = {
            id: 'kr-1',
            goalId: 'goal-123',
            title: 'Updated KR',
            type: 'ACCUMULATIVE',
            targetValue: 12,
            currentValue: 5,
            completed: false,
            createdAt: '2026-01-01T00:00:00Z',
            updatedAt: '2026-01-25T00:00:00Z',
        };
        vi.mocked(api.put).mockResolvedValueOnce(mockKeyResult);

        const { result } = renderHook(() => useUpdateKeyResult(), { wrapper: createWrapper() });

        result.current.mutate({
            goalId: 'goal-123',
            keyResultId: 'kr-1',
            updates: { currentValue: 5 },
        });

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        expect(api.put).toHaveBeenCalledWith('/api/goals/key-results/kr-1', { currentValue: 5 });
    });
});

describe('useDeleteKeyResult', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('calls DELETE /api/goals/key-results/{id}', async () => {
        vi.mocked(api.delete).mockResolvedValueOnce(undefined);

        const { result } = renderHook(() => useDeleteKeyResult(), { wrapper: createWrapper() });

        result.current.mutate({ goalId: 'goal-123', keyResultId: 'kr-1' });

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        expect(api.delete).toHaveBeenCalledWith('/api/goals/key-results/kr-1');
    });
});

describe('useCompleteMilestone', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('calls POST /api/goals/key-results/{id}/complete', async () => {
        const mockKeyResult: KeyResult = {
            id: 'kr-1',
            goalId: 'goal-123',
            title: 'Launch MVP',
            type: 'MILESTONE',
            targetValue: 1,
            currentValue: 1,
            completed: true,
            createdAt: '2026-01-01T00:00:00Z',
            updatedAt: '2026-01-25T00:00:00Z',
        };
        vi.mocked(api.post).mockResolvedValueOnce(mockKeyResult);

        const { result } = renderHook(() => useCompleteMilestone(), { wrapper: createWrapper() });

        result.current.mutate({ goalId: 'goal-123', keyResultId: 'kr-1' });

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        expect(api.post).toHaveBeenCalledWith('/api/goals/key-results/kr-1/complete');
        expect(result.current.data?.completed).toBe(true);
    });
});
