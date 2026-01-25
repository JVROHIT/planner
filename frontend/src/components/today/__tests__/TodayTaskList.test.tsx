import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { TodayTaskList } from '../TodayTaskList';
import type { DailyPlan } from '@/types/domain';

// Mock hooks
vi.mock('@/hooks', () => ({
  useCompleteTask: vi.fn(() => ({
    mutateAsync: vi.fn().mockResolvedValue(undefined),
    isPending: false,
  })),
  useMissTask: vi.fn(() => ({
    mutateAsync: vi.fn().mockResolvedValue(undefined),
    isPending: false,
  })),
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

describe('TodayTaskList', () => {
  const mockDailyPlan: DailyPlan = {
    id: 'plan-1',
    userId: 'user-123',
    day: '2026-01-25',
    tasks: [
      {
        taskId: 'task-1',
        task: {
          id: 'task-1',
          description: 'Task 1',
          userId: 'user-123',
          createdAt: '2026-01-25T00:00:00Z',
          updatedAt: '2026-01-25T00:00:00Z',
        },
        completed: false,
        missed: false,
      },
      {
        taskId: 'task-2',
        task: {
          id: 'task-2',
          description: 'Task 2',
          userId: 'user-123',
          createdAt: '2026-01-25T00:00:00Z',
          updatedAt: '2026-01-25T00:00:00Z',
        },
        completed: true,
        missed: false,
      },
    ],
    closed: false,
    createdAt: '2026-01-25T00:00:00Z',
    updatedAt: '2026-01-25T00:00:00Z',
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders list of TaskRow components', () => {
    const Wrapper = createWrapper();
    render(<TodayTaskList dailyPlan={mockDailyPlan} isLoading={false} />, { wrapper: Wrapper });

    expect(screen.getByText('Task 1')).toBeInTheDocument();
    expect(screen.getByText('Task 2')).toBeInTheDocument();
  });

  it('shows empty state when no tasks', () => {
    const emptyPlan: DailyPlan = {
      ...mockDailyPlan,
      tasks: [],
    };

    const Wrapper = createWrapper();
    render(<TodayTaskList dailyPlan={emptyPlan} isLoading={false} />, { wrapper: Wrapper });

    expect(screen.getByText(/no tasks for today/i)).toBeInTheDocument();
  });

  it('shows empty state when dailyPlan is null', () => {
    const Wrapper = createWrapper();
    render(<TodayTaskList dailyPlan={null} isLoading={false} />, { wrapper: Wrapper });

    expect(screen.getByText(/no tasks for today/i)).toBeInTheDocument();
  });

  it('shows loading skeleton while loading', () => {
    const Wrapper = createWrapper();
    render(<TodayTaskList dailyPlan={null} isLoading={true} />, { wrapper: Wrapper });

    const skeletons = screen.getAllByLabelText('Loading task');
    expect(skeletons.length).toBeGreaterThan(0);
  });
});
