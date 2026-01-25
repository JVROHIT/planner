import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { DayColumn } from '../DayColumn';
import type { DayProgress, Task } from '@/types/domain';

// Mock components
vi.mock('../WeekTaskItem', () => ({
  WeekTaskItem: ({ task }: { task: Task }) => <div>{task.description}</div>,
}));

vi.mock('../AddTaskDialog', () => ({
  AddTaskDialog: ({ isOpen }: { isOpen: boolean }) =>
    isOpen ? <div>Add Task Dialog</div> : null,
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

describe('DayColumn', () => {
  const mockDay: DayProgress = {
    date: '2026-01-25',
    dayOfWeek: 'MONDAY',
    completed: 2,
    total: 5,
    closed: false,
  };

  const mockTasks: Task[] = [
    {
      id: 'task-1',
      description: 'Task 1',
      userId: 'user-123',
      createdAt: '2026-01-25T00:00:00Z',
      updatedAt: '2026-01-25T00:00:00Z',
    },
    {
      id: 'task-2',
      description: 'Task 2',
      userId: 'user-123',
      createdAt: '2026-01-25T00:00:00Z',
      updatedAt: '2026-01-25T00:00:00Z',
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders date header', () => {
    const Wrapper = createWrapper();
    render(
      <DayColumn
        day={mockDay}
        dayOfWeek="MONDAY"
        tasks={[]}
        weekStart="2026-01-20"
        isEditable={true}
      />,
      { wrapper: Wrapper }
    );

    expect(screen.getByText('MON')).toBeInTheDocument();
  });

  it('renders task list', () => {
    const Wrapper = createWrapper();
    render(
      <DayColumn
        day={mockDay}
        dayOfWeek="MONDAY"
        tasks={mockTasks}
        weekStart="2026-01-20"
        isEditable={true}
      />,
      { wrapper: Wrapper }
    );

    expect(screen.getByText('Task 1')).toBeInTheDocument();
    expect(screen.getByText('Task 2')).toBeInTheDocument();
  });

  it('shows add task button for future days', async () => {
    const user = userEvent.setup();
    const Wrapper = createWrapper();
    render(
      <DayColumn
        day={mockDay}
        dayOfWeek="MONDAY"
        tasks={[]}
        weekStart="2026-01-20"
        isEditable={true}
      />,
      { wrapper: Wrapper }
    );

    const addButton = screen.getByRole('button', { name: /add task/i });
    expect(addButton).toBeInTheDocument();

    await user.click(addButton);
    expect(screen.getByText('Add Task Dialog')).toBeInTheDocument();
  });

  it('disables interactions for closed days', () => {
    const closedDay: DayProgress = {
      ...mockDay,
      closed: true,
    };

    const Wrapper = createWrapper();
    render(
      <DayColumn
        day={closedDay}
        dayOfWeek="MONDAY"
        tasks={mockTasks}
        weekStart="2026-01-20"
        isEditable={false}
      />,
      { wrapper: Wrapper }
    );

    expect(screen.queryByRole('button', { name: /add task/i })).not.toBeInTheDocument();
    expect(screen.getByText('Closed')).toBeInTheDocument();
  });

  it('applies frozen styling to past days', () => {
    const pastDay: DayProgress = {
      ...mockDay,
      date: '2026-01-01',
      closed: false,
    };

    const Wrapper = createWrapper();
    const { container } = render(
      <DayColumn
        day={pastDay}
        dayOfWeek="MONDAY"
        tasks={mockTasks}
        weekStart="2026-01-20"
        isEditable={false}
      />,
      { wrapper: Wrapper }
    );

    const column = container.firstChild;
    expect(column).toHaveClass('opacity-60');
  });

  it('shows completion stats when tasks exist', () => {
    const Wrapper = createWrapper();
    render(
      <DayColumn
        day={mockDay}
        dayOfWeek="MONDAY"
        tasks={mockTasks}
        weekStart="2026-01-20"
        isEditable={true}
      />,
      { wrapper: Wrapper }
    );

    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByText('/')).toBeInTheDocument();
    expect(screen.getByText('5')).toBeInTheDocument();
  });
});
