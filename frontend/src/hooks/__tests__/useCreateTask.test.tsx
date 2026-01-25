import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { useCreateTask } from '../useCreateTask';
import { api } from '@/lib/api';
import type { Task } from '@/types/domain';
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

describe('useCreateTask', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls POST /api/tasks', async () => {
    const mockTask: Task = {
      id: 'task-123',
      description: 'New task',
      userId: 'user-123',
      createdAt: '2026-01-25T00:00:00Z',
      updatedAt: '2026-01-25T00:00:00Z',
    };
    vi.mocked(api.post).mockResolvedValueOnce(mockTask);

    const { result } = renderHook(() => useCreateTask(), { wrapper: createWrapper() });

    await result.current.mutateAsync({
      description: 'New task',
    });

    expect(api.post).toHaveBeenCalledWith('/api/tasks', {
      description: 'New task',
    });
  });

  it('returns created task', async () => {
    const mockTask: Task = {
      id: 'task-123',
      description: 'New task',
      userId: 'user-123',
      createdAt: '2026-01-25T00:00:00Z',
      updatedAt: '2026-01-25T00:00:00Z',
    };
    vi.mocked(api.post).mockResolvedValueOnce(mockTask);

    const { result } = renderHook(() => useCreateTask(), { wrapper: createWrapper() });

    const task = await result.current.mutateAsync({
      description: 'New task',
    });

    expect(task).toEqual(mockTask);
    expect(task.id).toBe('task-123');
    expect(task.description).toBe('New task');
  });
});
