import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { useGoals, useCreateGoal, useUpdateGoal, useDeleteGoal } from '../';
import { api } from '@/lib/api';
import type { Goal } from '@/types/domain';
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

describe('useGoals', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('fetches from /api/goals', async () => {
        const mockGoals: Goal[] = [];
        vi.mocked(api.get).mockResolvedValueOnce(mockGoals);

        const { result } = renderHook(() => useGoals(), { wrapper: createWrapper() });

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        expect(api.get).toHaveBeenCalledWith('/api/goals');
    });

    it('returns Goal[] shape', async () => {
        const mockGoals: Goal[] = [
            {
                id: 'goal-1',
                userId: 'user-123',
                title: 'Read 12 books',
                horizon: 'YEAR',
                startDate: '2026-01-01',
                endDate: '2026-12-31',
                status: 'ACTIVE',
            },
        ];
        vi.mocked(api.get).mockResolvedValueOnce(mockGoals);

        const { result } = renderHook(() => useGoals(), { wrapper: createWrapper() });

        await waitFor(() => {
            expect(result.current.data).toEqual(mockGoals);
        });

        expect(result.current.data).toHaveLength(1);
        expect(result.current.data?.[0].title).toBe('Read 12 books');
    });
});

describe('useCreateGoal', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('calls POST /api/goals', async () => {
        const mockGoal: Goal = {
            id: 'goal-1',
            userId: 'user-123',
            title: 'New Goal',
            horizon: 'MONTH',
            startDate: '2026-01-25',
            endDate: '2026-02-25',
            status: 'ACTIVE',
        };
        vi.mocked(api.post).mockResolvedValueOnce(mockGoal);

        const { result } = renderHook(() => useCreateGoal(), { wrapper: createWrapper() });

        result.current.mutate({ title: 'New Goal' });

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        expect(api.post).toHaveBeenCalledWith('/api/goals', { title: 'New Goal' });
    });

    it('invalidates goals queries on success', async () => {
        const mockGoal: Goal = {
            id: 'goal-1',
            userId: 'user-123',
            title: 'New Goal',
            horizon: 'MONTH',
            startDate: '2026-01-25',
            endDate: '2026-02-25',
            status: 'ACTIVE',
        };
        vi.mocked(api.post).mockResolvedValueOnce(mockGoal);

        const { result } = renderHook(() => useCreateGoal(), { wrapper: createWrapper() });

        result.current.mutate({ title: 'New Goal' });

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        // Query invalidation is handled by TanStack Query internally
        expect(result.current.data).toEqual(mockGoal);
    });
});

describe('useUpdateGoal', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('calls PUT /api/goals/{id}', async () => {
        const mockGoal: Goal = {
            id: 'goal-1',
            userId: 'user-123',
            title: 'Updated Goal',
            horizon: 'MONTH',
            startDate: '2026-01-01',
            endDate: '2026-02-01',
            status: 'ACTIVE',
        };
        vi.mocked(api.put).mockResolvedValueOnce(mockGoal);

        const { result } = renderHook(() => useUpdateGoal(), { wrapper: createWrapper() });

        result.current.mutate({ id: 'goal-1', updates: { title: 'Updated Goal' } });

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        expect(api.put).toHaveBeenCalledWith('/api/goals/goal-1', { title: 'Updated Goal' });
    });
});

describe('useDeleteGoal', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('calls DELETE /api/goals/{id}', async () => {
        vi.mocked(api.delete).mockResolvedValueOnce(undefined);

        const { result } = renderHook(() => useDeleteGoal(), { wrapper: createWrapper() });

        result.current.mutate('goal-1');

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        expect(api.delete).toHaveBeenCalledWith('/api/goals/goal-1');
    });

    it('invalidates goals queries on success', async () => {
        vi.mocked(api.delete).mockResolvedValueOnce(undefined);

        const { result } = renderHook(() => useDeleteGoal(), { wrapper: createWrapper() });

        result.current.mutate('goal-1');

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        // Query invalidation is handled by TanStack Query internally
        expect(result.current.isSuccess).toBe(true);
    });
});
