import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { GoalSummaryCard } from '../GoalSummaryCard';
import type { GoalSummary } from '@/types/domain';

describe('GoalSummaryCard', () => {
  const mockSummary: GoalSummary = {
    goalId: 'goal-1',
    title: 'Test Goal',
    averageProgress: 0.75,
    status: 'ON_TRACK',
    trend: 'UP',
  };

  it('renders goal title', () => {
    render(<GoalSummaryCard summary={mockSummary} />);

    expect(screen.getByText('Test Goal')).toBeInTheDocument();
  });

  it('displays progress percentage', () => {
    render(<GoalSummaryCard summary={mockSummary} />);

    expect(screen.getByText('75%')).toBeInTheDocument();
  });

  it('renders progress bar', () => {
    render(<GoalSummaryCard summary={mockSummary} />);

    const progressBar = screen.getByRole('progressbar');
    expect(progressBar).toBeInTheDocument();
    expect(progressBar).toHaveAttribute('aria-valuenow', '75');
  });

  it('shows status indicator', () => {
    render(<GoalSummaryCard summary={mockSummary} />);

    expect(screen.getByText('On track')).toBeInTheDocument();
  });

  it('shows trend indicator', () => {
    render(<GoalSummaryCard summary={mockSummary} />);

    expect(screen.getByLabelText('Trending up')).toBeInTheDocument();
  });

  it('links to goals page', () => {
    render(<GoalSummaryCard summary={mockSummary} />);

    const link = screen.getByRole('link');
    expect(link).toHaveAttribute('href', '/goals');
  });

  it('displays AHEAD status correctly', () => {
    const aheadSummary: GoalSummary = {
      ...mockSummary,
      status: 'AHEAD',
    };

    render(<GoalSummaryCard summary={aheadSummary} />);

    expect(screen.getByText('Ahead')).toBeInTheDocument();
  });

  it('displays BEHIND status correctly', () => {
    const behindSummary: GoalSummary = {
      ...mockSummary,
      status: 'BEHIND',
      trend: 'DOWN',
    };

    render(<GoalSummaryCard summary={behindSummary} />);

    expect(screen.getByText('Behind')).toBeInTheDocument();
    expect(screen.getByLabelText('Trending down')).toBeInTheDocument();
  });
});
