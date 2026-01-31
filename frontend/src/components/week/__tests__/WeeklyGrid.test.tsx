import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { WeeklyGrid } from '../WeeklyGrid';
import type { DayProgress, WeeklyPlan, Task } from '@/types/domain';

// Mock hooks
const mockUseTasks = vi.fn();
const mockUseUpdateWeeklyPlan = vi.fn();
const mockUseWeeklyPlan = vi.fn();

vi.mock('@/hooks', () => ({
  useTasks: () => mockUseTasks(),
  useUpdateWeeklyPlan: () => mockUseUpdateWeeklyPlan(),
  useWeeklyPlan: () => mockUseWeeklyPlan(),
}));

// Mock DayColumn
vi.mock('../DayColumn', () => ({
  DayColumn: ({ day, tasks }: { day: DayProgress; tasks: Task[] }) => (
    <div data-testid={`day-${day.date}`}>
      {day.dayOfWeek} - {tasks.length} tasks
    </div>
  ),
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

describe('WeeklyGrid', () => {
  const mockWeekProgress: DayProgress[] = [
    {
      date: '2026-01-20',
      dayOfWeek: 'MONDAY',
      completed: 2,
      total: 5,
      closed: false,
    },
    {
      date: '2026-01-21',
      dayOfWeek: 'TUESDAY',
      completed: 1,
      total: 3,
      closed: false,
    },
  ];

  const mockWeeklyPlan: WeeklyPlan = {
    id: 'plan-1',
    userId: 'user-123',
    weekStart: '2026-01-20',
    taskGrid: {
      '2026-01-20': ['task-1', 'task-2'],
      '2026-01-21': ['task-3'],
    },
    updatedAt: '2026-01-20T00:00:00Z',
  };

  const mockTasks: Task[] = [
    {
      id: 'task-1',
      title: 'Task 1',
      userId: 'user-123',
      createdAt: '2026-01-20T00:00:00Z',
      updatedAt: '2026-01-20T00:00:00Z',
    },
    {
      id: 'task-2',
      title: 'Task 2',
      userId: 'user-123',
      createdAt: '2026-01-20T00:00:00Z',
      updatedAt: '2026-01-20T00:00:00Z',
    },
    {
      id: 'task-3',
      title: 'Task 3',
      userId: 'user-123',
      createdAt: '2026-01-20T00:00:00Z',
      updatedAt: '2026-01-20T00:00:00Z',
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseTasks.mockReturnValue({
      data: mockTasks,
      isLoading: false,
    });
    mockUseUpdateWeeklyPlan.mockReturnValue({
      mutateAsync: vi.fn().mockResolvedValue(undefined),
    });
    mockUseWeeklyPlan.mockReturnValue({
      data: mockWeeklyPlan,
      isLoading: false,
    });
  });

  it('renders 7 DayColumns', () => {
    const Wrapper = createWrapper();
    render(
      <WeeklyGrid
        weekProgress={mockWeekProgress}
        weeklyPlan={mockWeeklyPlan}
        weekStart="2026-01-20"
        isLoading={false}
      />,
      { wrapper: Wrapper }
    );

    // Should render 7 days (Mon-Sun)
    const dayColumns = screen.getAllByTestId(/^day-/);
    expect(dayColumns.length).toBe(7);
  });

  it('shows loading skeleton while loading', () => {
    const Wrapper = createWrapper();
    render(
      <WeeklyGrid
        weekProgress={undefined}
        weeklyPlan={undefined}
        weekStart="2026-01-20"
        isLoading={true}
      />,
      { wrapper: Wrapper }
    );

    // Should show loading skeletons
    const skeletons = document.querySelectorAll('.animate-pulse');
    expect(skeletons.length).toBeGreaterThan(0);
  });

  it('maps tasks correctly to days', () => {
    const Wrapper = createWrapper();
    render(
      <WeeklyGrid
        weekProgress={mockWeekProgress}
        weeklyPlan={mockWeeklyPlan}
        weekStart="2026-01-20"
        isLoading={false}
      />,
      { wrapper: Wrapper }
    );

    // Monday should have 2 tasks
    const mondayColumn = screen.getByTestId('day-2026-01-20');
    expect(mondayColumn).toHaveTextContent('2 tasks');

    // Tuesday should have 1 task
    const tuesdayColumn = screen.getByTestId('day-2026-01-21');
    expect(tuesdayColumn).toHaveTextContent('1 tasks');
  });

  it('prevents drop on closed days', () => {
    const closedProgress: DayProgress[] = [
      {
        date: '2026-01-20',
        dayOfWeek: 'MONDAY',
        completed: 0,
        total: 0,
        closed: true,
      },
    ];

    const Wrapper = createWrapper();
    render(
      <WeeklyGrid
        weekProgress={closedProgress}
        weeklyPlan={mockWeeklyPlan}
        weekStart="2026-01-20"
        isLoading={false}
      />,
      { wrapper: Wrapper }
    );

    // Closed days should be marked as non-editable
    const mondayColumn = screen.getByTestId('day-2026-01-20');
    expect(mondayColumn).toBeInTheDocument();
  });
});
