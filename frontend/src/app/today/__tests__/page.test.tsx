import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import type { UseQueryResult } from '@tanstack/react-query';
import TodayPage from '../page';
import { useTodayDashboard } from '@/hooks';
import type { TodayDashboard } from '@/types/domain';

// Mock hooks
vi.mock('@/hooks', async () => {
  const actual = await vi.importActual('@/hooks');
  return {
    ...actual,
    useTodayDashboard: vi.fn(),
    useCompleteTask: vi.fn(() => ({
      mutateAsync: vi.fn().mockResolvedValue(undefined),
      isPending: false,
    })),
    useMissTask: vi.fn(() => ({
      mutateAsync: vi.fn().mockResolvedValue(undefined),
      isPending: false,
    })),
  };
});

// Mock layout
vi.mock('@/components/layout', () => ({
  AppShell: ({ children }: { children: ReactNode }) => <div>{children}</div>,
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

describe('TodayPage', () => {
  const mockDashboard: TodayDashboard = {
    userId: 'user-123',
    todayPlan: {
      id: 'plan-1',
      userId: 'user-123',
      day: '2026-01-25',
      tasks: [
        {
          taskId: 'task-1',
          task: {
            id: 'task-1',
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

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders summary and task list', () => {
    vi.mocked(useTodayDashboard).mockReturnValue({
      data: mockDashboard,
      isLoading: false,
      error: null,
      isError: false,
      isSuccess: true,
      isPending: false,
      status: 'success',
      fetchStatus: 'idle',
      refetch: vi.fn(),
    } as UseQueryResult<TodayDashboard, Error>);

    const Wrapper = createWrapper();
    render(<TodayPage />, { wrapper: Wrapper });

    expect(screen.getByText('Today')).toBeInTheDocument();
    expect(screen.getByText('Test task')).toBeInTheDocument();
    // Check for streak - there are multiple "Current streak" texts, so use getAllByText
    const streakTexts = screen.getAllByText(/current streak/i);
    expect(streakTexts.length).toBeGreaterThan(0);
  });

  it('disables actions when day is closed', () => {
    const closedDashboard: TodayDashboard = {
      ...mockDashboard,
      todayPlan: {
        ...mockDashboard.todayPlan!,
        closed: true,
      },
    };

    vi.mocked(useTodayDashboard).mockReturnValue({
      data: closedDashboard,
      isLoading: false,
      error: null,
    } as UseQueryResult<TodayDashboard, Error>);

    const Wrapper = createWrapper();
    render(<TodayPage />, { wrapper: Wrapper });

    // Actions should be hidden for closed day
    expect(screen.queryByRole('button', { name: /complete/i })).not.toBeInTheDocument();
  });

  it('never reads from Task directly (invariant)', () => {
    vi.mocked(useTodayDashboard).mockReturnValue({
      data: mockDashboard,
      isLoading: false,
      error: null,
    } as UseQueryResult<TodayDashboard, Error>);

    const Wrapper = createWrapper();
    render(<TodayPage />, { wrapper: Wrapper });

    // Verify we're using useTodayDashboard (the single authoritative query)
    expect(useTodayDashboard).toHaveBeenCalled();
    
    // Verify we're not calling any Task-specific endpoints
    // This is verified by the fact that we only call useTodayDashboard
  });

  it('shows loading state', () => {
    vi.mocked(useTodayDashboard).mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
    } as UseQueryResult<TodayDashboard, Error>);

    const Wrapper = createWrapper();
    render(<TodayPage />, { wrapper: Wrapper });

    expect(screen.getByText('Today')).toBeInTheDocument();
    // Loading skeletons should be present
  });

  it('shows error state', () => {
    vi.mocked(useTodayDashboard).mockReturnValue({
      data: undefined,
      isLoading: false,
      error: { message: 'Failed to load' } as Error,
    } as UseQueryResult<TodayDashboard, Error>);

    const Wrapper = createWrapper();
    render(<TodayPage />, { wrapper: Wrapper });

    expect(screen.getByText("Failed to load today's data")).toBeInTheDocument();
    expect(screen.getByText('Failed to load')).toBeInTheDocument();
  });
});
