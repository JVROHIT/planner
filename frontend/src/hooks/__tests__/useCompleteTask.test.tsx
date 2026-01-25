import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { useCompleteTask } from '../useCompleteTask';
import { api } from '@/lib/api';
import type { TodayDashboard } from '@/types/domain';

// Mock next/navigation
const mockPush = vi.fn();
vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: mockPush }),
}));

// Mock API
vi.mock('@/lib/api', () => ({
  api: {
    post: vi.fn(),
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

describe('useCompleteTask', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls POST /api/daily/{date}/tasks/{taskId}/complete', async () => {
    vi.mocked(api.post).mockResolvedValueOnce(undefined);

    const { result } = renderHook(() => useCompleteTask(), { wrapper: createWrapper() });

    await result.current.mutateAsync({ taskId: 'task-123', date: '2026-01-25' });

    expect(api.post).toHaveBeenCalledWith('/api/daily/2026-01-25/tasks/task-123/complete');
  });

  it('invalidates today dashboard on success', async () => {
    vi.mocked(api.post).mockResolvedValueOnce(undefined);

    const queryClient = new QueryClient();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');

    const { result } = renderHook(() => useCompleteTask(), {
      wrapper: ({ children }: { children: ReactNode }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
      ),
    });

    await result.current.mutateAsync({ taskId: 'task-123', date: '2026-01-25' });

    await waitFor(() => {
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ['today-dashboard'] });
    });
  });

  it('performs optimistic update', async () => {
    vi.mocked(api.post).mockResolvedValueOnce(undefined);

    const mockDashboard: TodayDashboard = {
      userId: 'user-123',
      todayPlan: {
        id: 'plan-1',
        userId: 'user-123',
        day: '2026-01-25',
        tasks: [
          {
            taskId: 'task-123',
            task: {
              id: 'task-123',
              description: 'Test task',
              userId: 'user-123',
              createdAt: '2026-01-25T00:00:00Z',
              updatedAt: '2026-01-25T00:00:00Z',
            },
            completed: false,
            missed: false,
          },
        ],
        closed: false,
        createdAt: '2026-01-25T00:00:00Z',
        updatedAt: '2026-01-25T00:00:00Z',
      },
      completionRatio: 0,
      currentStreak: 0,
      goalSummaries: [],
    };

    const queryClient = new QueryClient();
    queryClient.setQueryData(['today-dashboard'], mockDashboard);

    const { result } = renderHook(() => useCompleteTask(), {
      wrapper: ({ children }: { children: ReactNode }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
      ),
    });

    result.current.mutate({ taskId: 'task-123', date: '2026-01-25' });

    await waitFor(() => {
      const updated = queryClient.getQueryData<TodayDashboard>(['today-dashboard']);
      expect(updated?.todayPlan?.tasks[0]?.completed).toBe(true);
    });
  });

  it('rolls back on error', async () => {
    const { ApiError } = await import('@/lib/api');
    vi.mocked(api.post).mockRejectedValueOnce(
      new ApiError('Task not found', 404, 'NOT_FOUND')
    );

    const mockDashboard: TodayDashboard = {
      userId: 'user-123',
      todayPlan: {
        id: 'plan-1',
        userId: 'user-123',
        day: '2026-01-25',
        tasks: [
          {
            taskId: 'task-123',
            task: {
              id: 'task-123',
              description: 'Test task',
              userId: 'user-123',
              createdAt: '2026-01-25T00:00:00Z',
              updatedAt: '2026-01-25T00:00:00Z',
            },
            completed: false,
            missed: false,
          },
        ],
        closed: false,
        createdAt: '2026-01-25T00:00:00Z',
        updatedAt: '2026-01-25T00:00:00Z',
      },
      completionRatio: 0,
      currentStreak: 0,
      goalSummaries: [],
    };

    const queryClient = new QueryClient();
    queryClient.setQueryData(['today-dashboard'], mockDashboard);

    const { result } = renderHook(() => useCompleteTask(), {
      wrapper: ({ children }: { children: ReactNode }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
      ),
    });

    result.current.mutate({ taskId: 'task-123', date: '2026-01-25' });

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
      // Should rollback to original state
      const rolledBack = queryClient.getQueryData<TodayDashboard>(['today-dashboard']);
      expect(rolledBack?.todayPlan?.tasks[0]?.completed).toBe(false);
    });
  });
});
