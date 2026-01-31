import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { useUpdateWeeklyPlan } from '../useUpdateWeeklyPlan';
import { api } from '@/lib/api';
import type { WeeklyPlan } from '@/types/domain';
import { ToastProvider } from '@/providers/ToastProvider';

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

describe('useUpdateWeeklyPlan', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls POST /api/weekly-plan', async () => {
    const mockPlan: WeeklyPlan = {
      id: 'plan-1',
      userId: 'user-123',
      weekStart: '2026-01-20',
      taskGrid: {
        '2026-01-20': ['task-1'],
      },
      updatedAt: '2026-01-20T00:00:00Z',
    };
    vi.mocked(api.post).mockResolvedValueOnce(mockPlan);

    const { result } = renderHook(() => useUpdateWeeklyPlan(), {
      wrapper: createWrapper(),
    });

    await result.current.mutateAsync({
      weekStart: '2026-01-20',
      taskGrid: {
        '2026-01-20': ['task-1'],
      },
    });

    expect(api.post).toHaveBeenCalledWith('/api/weekly-plan', {
      weekStart: '2026-01-20',
      taskGrid: {
        '2026-01-20': ['task-1'],
      },
    });
  });

  it('invalidates week queries on success', async () => {
    const mockPlan: WeeklyPlan = {
      id: 'plan-1',
      userId: 'user-123',
      weekStart: '2026-01-20',
      taskGrid: {
        '2026-01-20': [],
      },
      updatedAt: '2026-01-20T00:00:00Z',
    };
    vi.mocked(api.post).mockResolvedValueOnce(mockPlan);

    const queryClient = new QueryClient();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');

    const { result } = renderHook(() => useUpdateWeeklyPlan(), {
      wrapper: ({ children }: { children: ReactNode }) => (
        <QueryClientProvider client={queryClient}>
          <ToastProvider>
            {children}
          </ToastProvider>
        </QueryClientProvider>
      ),
    });

    await result.current.mutateAsync({
      weekStart: '2026-01-20',
      taskGrid: {
        '2026-01-20': [],
      },
    });

    await waitFor(() => {
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ['weekly-plan'] });
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ['week-dashboard'] });
    });
  });
});
