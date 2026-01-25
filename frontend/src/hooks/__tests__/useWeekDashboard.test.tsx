import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { useWeekDashboard } from '../useWeekDashboard';
import { api } from '@/lib/api';
import type { DayProgress } from '@/types/domain';

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

describe('useWeekDashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches from /api/dashboard/week with weekStart param', async () => {
    const mockBackendData = [
      {
        date: '2026-01-20',
        totalTasks: 5,
        completedTasks: 3,
        closed: false,
      },
    ];
    vi.mocked(api.get).mockResolvedValueOnce(mockBackendData);

    const { result } = renderHook(() => useWeekDashboard('2026-01-20'), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(api.get).toHaveBeenCalledWith('/api/dashboard/week?weekStart=2026-01-20');
  });

  it('returns DayProgress[] shape with transformed data', async () => {
    const mockBackendData = [
      {
        date: '2026-01-20',
        totalTasks: 5,
        completedTasks: 3,
        closed: false,
      },
    ];
    vi.mocked(api.get).mockResolvedValueOnce(mockBackendData);

    const { result } = renderHook(() => useWeekDashboard('2026-01-20'), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.data).toBeDefined();
    });

    expect(result.current.data?.[0]).toMatchObject({
      date: '2026-01-20',
      completed: 3,
      total: 5,
      closed: false,
    });
    expect(result.current.data?.[0].dayOfWeek).toBeDefined();
  });
});
