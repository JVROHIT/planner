import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TodaySummary } from '../TodaySummary';
import type { TodayDashboard } from '@/types/domain';

describe('TodaySummary', () => {
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
            description: 'Task 1',
            userId: 'user-123',
            createdAt: '2026-01-25T00:00:00Z',
            updatedAt: '2026-01-25T00:00:00Z',
          },
          completed: true,
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
          completed: false,
          missed: false,
        },
      ],
      closed: false,
      createdAt: '2026-01-25T00:00:00Z',
      updatedAt: '2026-01-25T00:00:00Z',
    },
    completionRatio: 0.5,
    currentStreak: 5,
    goalSummaries: [],
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('displays completion count', () => {
    render(<TodaySummary dashboard={mockDashboard} isLoading={false} />);

    expect(screen.getByText('1')).toBeInTheDocument(); // completed
    expect(screen.getByText('/ 2')).toBeInTheDocument(); // total
  });

  it('displays percentage', () => {
    render(<TodaySummary dashboard={mockDashboard} isLoading={false} />);

    expect(screen.getByText(/50% complete/i)).toBeInTheDocument();
  });

  it('displays current streak', () => {
    render(<TodaySummary dashboard={mockDashboard} isLoading={false} />);

    expect(screen.getByText(/current streak/i)).toBeInTheDocument();
    expect(screen.getByText('5 days')).toBeInTheDocument();
  });

  it('shows closed indicator when day is closed', () => {
    const closedDashboard: TodayDashboard = {
      ...mockDashboard,
      todayPlan: {
        ...mockDashboard.todayPlan!,
        closed: true,
      },
    };

    render(<TodaySummary dashboard={closedDashboard} isLoading={false} />);

    expect(screen.getByText('Closed')).toBeInTheDocument();
  });

  it('shows loading skeleton while loading', () => {
    render(<TodaySummary dashboard={undefined} isLoading={true} />);

    // Should show loading skeleton (animated pulse)
    const skeleton = document.querySelector('.animate-pulse');
    expect(skeleton).toBeInTheDocument();
  });
});
