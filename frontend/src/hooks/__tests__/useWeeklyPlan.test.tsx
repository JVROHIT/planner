import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { useWeeklyPlan } from '../useWeeklyPlan';
import { api } from '@/lib/api';
import type { WeeklyPlan } from '@/types/domain';

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

describe('useWeeklyPlan', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches from /api/weekly-plan/{date}', async () => {
    const mockPlan: WeeklyPlan = {
      id: 'plan-1',
      userId: 'user-123',
      weekNumber: 4,
      year: 2026,
      weekStartDate: '2026-01-20',
      taskGrid: {
        MONDAY: [],
        TUESDAY: [],
        WEDNESDAY: [],
        THURSDAY: [],
        FRIDAY: [],
        SATURDAY: [],
        SUNDAY: [],
      },
      createdAt: '2026-01-20T00:00:00Z',
      updatedAt: '2026-01-20T00:00:00Z',
    };
    vi.mocked(api.get).mockResolvedValueOnce(mockPlan);

    const { result } = renderHook(() => useWeeklyPlan('2026-01-25'), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(api.get).toHaveBeenCalledWith('/api/weekly-plan/2026-01-25');
  });

  it('returns WeeklyPlan shape', async () => {
    const mockPlan: WeeklyPlan = {
      id: 'plan-1',
      userId: 'user-123',
      weekNumber: 4,
      year: 2026,
      weekStartDate: '2026-01-20',
      taskGrid: {
        MONDAY: ['task-1'],
        TUESDAY: [],
        WEDNESDAY: [],
        THURSDAY: [],
        FRIDAY: [],
        SATURDAY: [],
        SUNDAY: [],
      },
      createdAt: '2026-01-20T00:00:00Z',
      updatedAt: '2026-01-20T00:00:00Z',
    };
    vi.mocked(api.get).mockResolvedValueOnce(mockPlan);

    const { result } = renderHook(() => useWeeklyPlan('2026-01-25'), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.data).toEqual(mockPlan);
    });

    expect(result.current.data?.weekNumber).toBe(4);
    expect(result.current.data?.taskGrid.MONDAY).toEqual(['task-1']);
  });
});
