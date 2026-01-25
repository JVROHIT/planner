import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { useMissTask } from '../useMissTask';
import { api } from '@/lib/api';
import { ToastProvider } from '@/providers/ToastProvider';

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

describe('useMissTask', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls POST /api/daily/{date}/tasks/{taskId}/miss', async () => {
    vi.mocked(api.post).mockResolvedValueOnce(undefined);

    const { result } = renderHook(() => useMissTask(), { wrapper: createWrapper() });

    await result.current.mutateAsync({ taskId: 'task-123', date: '2026-01-25' });

    expect(api.post).toHaveBeenCalledWith('/api/daily/2026-01-25/tasks/task-123/miss');
  });

  it('invalidates today dashboard on success', async () => {
    vi.mocked(api.post).mockResolvedValueOnce(undefined);

    const queryClient = new QueryClient();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');

    const { result } = renderHook(() => useMissTask(), {
      wrapper: ({ children }: { children: ReactNode }) => (
        <QueryClientProvider client={queryClient}>
          <ToastProvider>
            {children}
          </ToastProvider>
        </QueryClientProvider>
      ),
    });

    await result.current.mutateAsync({ taskId: 'task-123', date: '2026-01-25' });

    await waitFor(() => {
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ['today-dashboard'] });
    });
  });
});
