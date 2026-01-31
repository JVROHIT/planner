import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { useGoalsDashboard } from '../useGoalsDashboard';
import { api } from '@/lib/api';
import type { GoalsDashboard } from '@/types/domain';

// Mock API
vi.mock('@/lib/api', () => ({
    api: {
        get: vi.fn(),
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

// Create wrapper with QueryClient
const createWrapper = () => {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: { retry: false },
            mutations: { retry: false },
        },
    });
    const Wrapper = ({ children }: { children: ReactNode }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );
    Wrapper.displayName = 'QueryClientWrapper';
    return Wrapper;
};

describe('useGoalsDashboard', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('fetches from /api/dashboard/goals', async () => {
        const mockDashboard: GoalsDashboard = {
            goals: [],
        };
        vi.mocked(api.get).mockResolvedValueOnce(mockDashboard);

        const { result } = renderHook(() => useGoalsDashboard(), { wrapper: createWrapper() });

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        expect(api.get).toHaveBeenCalledWith('/api/dashboard/goals');
    });

    it('returns GoalDetail[] with snapshots and trends', async () => {
        const mockDashboard: GoalsDashboard = {
            goals: [
                {
                    goal: {
                        id: 'goal-1',
                        userId: 'user-123',
                        title: 'Read 12 books',
                        horizon: 'YEAR',
                        startDate: '2026-01-01',
                        endDate: '2026-12-31',
                        status: 'ACTIVE',
                    },
                    keyResults: [],
                    latestSnapshot: {
                        id: 'snapshot-1',
                        goalId: 'goal-1',
                        date: '2026-01-25',
                        actual: 3,
                        expected: 2,
                    },
                    status: 'AHEAD',
                    trend: 'UP',
                    actualPercent: 25.0,
                    expectedPercent: 16.7,
                },
            ],
        };
        vi.mocked(api.get).mockResolvedValueOnce(mockDashboard);

        const { result } = renderHook(() => useGoalsDashboard(), { wrapper: createWrapper() });

        await waitFor(() => {
            expect(result.current.data).toEqual(mockDashboard);
        });

        expect(result.current.data?.goals).toHaveLength(1);
        expect(result.current.data?.goals[0].status).toBe('AHEAD');
        expect(result.current.data?.goals[0].trend).toBe('UP');
    });

    it('handles loading state', () => {
        vi.mocked(api.get).mockImplementation(() => new Promise(() => { })); // Never resolves

        const { result } = renderHook(() => useGoalsDashboard(), { wrapper: createWrapper() });

        expect(result.current.isLoading).toBe(true);
        expect(result.current.data).toBeUndefined();
    });

    it('handles error state', async () => {
        const { ApiError } = await import('@/lib/api');
        vi.mocked(api.get).mockRejectedValueOnce(
            new ApiError('Failed to fetch goals', 500, 'INTERNAL_ERROR')
        );

        const { result } = renderHook(() => useGoalsDashboard(), { wrapper: createWrapper() });

        await waitFor(() => {
            expect(result.current.isError).toBe(true);
        });

        expect(result.current.error).toBeDefined();
    });

    it('never computes progress client-side (invariant)', async () => {
        const mockDashboard: GoalsDashboard = {
            goals: [
                {
                    goal: {
                        id: 'goal-1',
                        userId: 'user-123',
                        title: 'Test Goal',
                        active: true,
                        createdAt: '2026-01-01T00:00:00Z',
                        updatedAt: '2026-01-01T00:00:00Z',
                    },
                    keyResults: [],
                    latestSnapshot: null,
                    status: 'ON_TRACK',
                    trend: 'FLAT',
                    actualPercent: 50.0,
                    expectedPercent: 50.0,
                },
            ],
        };
        vi.mocked(api.get).mockResolvedValueOnce(mockDashboard);

        const { result } = renderHook(() => useGoalsDashboard(), { wrapper: createWrapper() });

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        // Verify we render backend-provided values, not computed
        expect(result.current.data?.goals[0].actualPercent).toBe(50.0);
        expect(result.current.data?.goals[0].expectedPercent).toBe(50.0);
        expect(result.current.data?.goals[0].status).toBe('ON_TRACK');
        expect(result.current.data?.goals[0].trend).toBe('FLAT');
    });
});
