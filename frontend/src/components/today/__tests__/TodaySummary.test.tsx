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
      entries: [
        {
          taskId: 'task-1',
          title: 'Task 1',
          status: 'COMPLETED',
        },
        {
          taskId: 'task-2',
          title: 'Task 2',
          status: 'PENDING',
        },
      ],
      closed: false,
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
