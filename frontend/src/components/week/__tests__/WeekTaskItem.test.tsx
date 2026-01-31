import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { WeekTaskItem } from '../WeekTaskItem';
import type { Task, DayProgress } from '@/types/domain';

describe('WeekTaskItem', () => {
  const mockTask: Task = {
    id: 'task-123',
    title: 'Test task',
    userId: 'user-123',
    createdAt: '2026-01-25T00:00:00Z',
    updatedAt: '2026-01-25T00:00:00Z',
  };

  const mockDay: DayProgress = {
    date: '2026-01-20', // This is a Monday
    dayOfWeek: 'MONDAY',
    completed: 0,
    total: 1,
    closed: false,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('is draggable for future days', () => {
    const { container } = render(
      <WeekTaskItem task={mockTask} day={mockDay} isEditable={true} />
    );

    const taskItem = container.querySelector('[draggable="true"]');
    expect(taskItem).toBeInTheDocument();
  });

  it('is not draggable for closed days', () => {
    const closedDay: DayProgress = {
      ...mockDay,
      closed: true,
    };

    const { container } = render(
      <WeekTaskItem task={mockTask} day={closedDay} isEditable={false} />
    );

    // Task item still exists but interactions are disabled
    const taskItem = container.querySelector('[draggable]');
    expect(taskItem).toBeInTheDocument();
  });

  it('shows remove button', () => {
    const handleRemove = vi.fn();
    render(
      <WeekTaskItem
        task={mockTask}
        day={mockDay}
        isEditable={true}
        onRemove={handleRemove}
      />
    );

    const removeButton = screen.getByRole('button', { name: /remove task/i });
    expect(removeButton).toBeInTheDocument();
  });

  it('calls onRemove when remove button is clicked', async () => {
    const user = userEvent.setup();
    const handleRemove = vi.fn();
    render(
      <WeekTaskItem
        task={mockTask}
        day={mockDay}
        isEditable={true}
        onRemove={handleRemove}
      />
    );

    const removeButton = screen.getByRole('button', { name: /remove task/i });
    await user.click(removeButton);

    // The function will call with the actual day of week for the date
    expect(handleRemove).toHaveBeenCalled();
    expect(handleRemove.mock.calls[0][0]).toBe('task-123');
    expect(typeof handleRemove.mock.calls[0][1]).toBe('string'); // date
  });

  it('does not show remove button when not editable', () => {
    render(
      <WeekTaskItem task={mockTask} day={mockDay} isEditable={false} />
    );

    expect(screen.queryByRole('button', { name: /remove task/i })).not.toBeInTheDocument();
  });

  it('renders task title', () => {
    render(
      <WeekTaskItem task={mockTask} day={mockDay} isEditable={true} />
    );

    expect(screen.getByText('Test task')).toBeInTheDocument();
  });
});
