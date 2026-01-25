import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { useTodayDashboard } from '../useTodayDashboard';
import { api } from '@/lib/api';
import type { TodayDashboard } from '@/types/domain';

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

describe('useTodayDashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches from /api/dashboard/today', async () => {
    const mockDashboard: TodayDashboard = {
      userId: 'user-123',
      todayPlan: null,
      completionRatio: 0,
      currentStreak: 0,
      goalSummaries: [],
    };
    vi.mocked(api.get).mockResolvedValueOnce(mockDashboard);

    const { result } = renderHook(() => useTodayDashboard(), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(api.get).toHaveBeenCalledWith('/api/dashboard/today');
  });

  it('returns TodayDashboard shape', async () => {
    const mockDashboard: TodayDashboard = {
      userId: 'user-123',
      todayPlan: {
        id: 'plan-1',
        userId: 'user-123',
        day: '2026-01-25',
        tasks: [],
        closed: false,
        createdAt: '2026-01-25T00:00:00Z',
        updatedAt: '2026-01-25T00:00:00Z',
      },
      completionRatio: 0.5,
      currentStreak: 5,
      goalSummaries: [
        {
          goalId: 'goal-1',
          title: 'Test Goal',
          averageProgress: 0.75,
          status: 'ON_TRACK',
          trend: 'UP',
        },
      ],
    };
    vi.mocked(api.get).mockResolvedValueOnce(mockDashboard);

    const { result } = renderHook(() => useTodayDashboard(), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.data).toEqual(mockDashboard);
    });

    expect(result.current.data?.userId).toBe('user-123');
    expect(result.current.data?.completionRatio).toBe(0.5);
    expect(result.current.data?.currentStreak).toBe(5);
  });

  it('handles loading state', () => {
    vi.mocked(api.get).mockImplementation(() => new Promise(() => {})); // Never resolves

    const { result } = renderHook(() => useTodayDashboard(), { wrapper: createWrapper() });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.data).toBeUndefined();
  });

  it('handles error state', async () => {
    const { ApiError } = await import('@/lib/api');
    vi.mocked(api.get).mockRejectedValueOnce(
      new ApiError('Failed to fetch', 500, 'INTERNAL_ERROR')
    );

    const { result } = renderHook(() => useTodayDashboard(), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });

    expect(result.current.error).toBeDefined();
  });
});
