import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { TaskRow } from '../TaskRow';
import type { TaskExecution } from '@/types/domain';

// Mock hooks
const mockUseCompleteTask = vi.fn();
const mockUseMissTask = vi.fn();

vi.mock('@/hooks', () => ({
  useCompleteTask: () => mockUseCompleteTask(),
  useMissTask: () => mockUseMissTask(),
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

describe('TaskRow', () => {
  const mockTask: TaskExecution = {
    taskId: 'task-123',
    task: {
      id: 'task-123',
      description: 'Test task',
      userId: 'user-123',
      createdAt: '2026-01-25T00:00:00Z',
      updatedAt: '2026-01-25T00:00:00Z',
    },
    completed: false,
    missed: false,
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseCompleteTask.mockReturnValue({
      mutateAsync: vi.fn().mockResolvedValue(undefined),
      isPending: false,
    });
    mockUseMissTask.mockReturnValue({
      mutateAsync: vi.fn().mockResolvedValue(undefined),
      isPending: false,
    });
  });

  it('renders task title', () => {
    const Wrapper = createWrapper();
    render(
      <TaskRow task={mockTask} date="2026-01-25" dayClosed={false} />,
      { wrapper: Wrapper }
    );

    expect(screen.getByText('Test task')).toBeInTheDocument();
  });

  it('shows complete button for pending task', () => {
    const Wrapper = createWrapper();
    render(
      <TaskRow task={mockTask} date="2026-01-25" dayClosed={false} />,
      { wrapper: Wrapper }
    );

    expect(screen.getByRole('button', { name: /complete/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /miss/i })).toBeInTheDocument();
  });

  it('hides actions when day is closed', () => {
    const Wrapper = createWrapper();
    render(
      <TaskRow task={mockTask} date="2026-01-25" dayClosed={true} />,
      { wrapper: Wrapper }
    );

    expect(screen.queryByRole('button', { name: /complete/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /miss/i })).not.toBeInTheDocument();
  });

  it('applies completed styling when completed', () => {
    const completedTask: TaskExecution = {
      ...mockTask,
      completed: true,
      missed: false,
    };

    const Wrapper = createWrapper();
    const { container } = render(
      <TaskRow task={completedTask} date="2026-01-25" dayClosed={false} />,
      { wrapper: Wrapper }
    );

    // Find the outer container div (first div with flex items-center)
    const row = container.querySelector('div.flex.items-center.gap-3');
    expect(row).toHaveClass('bg-success/10');
  });

  it('applies missed styling when missed', () => {
    const missedTask: TaskExecution = {
      ...mockTask,
      completed: false,
      missed: true,
    };

    const Wrapper = createWrapper();
    const { container } = render(
      <TaskRow task={missedTask} date="2026-01-25" dayClosed={false} />,
      { wrapper: Wrapper }
    );

    // Find the outer container div (first div with flex items-center)
    const row = container.querySelector('div.flex.items-center.gap-3');
    expect(row).toHaveClass('bg-muted/50');
  });

  it('calls completeTask on Complete button click', async () => {
    const user = userEvent.setup();
    const mockComplete = vi.fn().mockResolvedValue(undefined);
    mockUseCompleteTask.mockReturnValue({
      mutateAsync: mockComplete,
      isPending: false,
    });

    const Wrapper = createWrapper();
    render(
      <TaskRow task={mockTask} date="2026-01-25" dayClosed={false} />,
      { wrapper: Wrapper }
    );

    const completeButton = screen.getByRole('button', { name: /complete/i });
    await user.click(completeButton);

    await waitFor(() => {
      expect(mockComplete).toHaveBeenCalledWith({ taskId: 'task-123', date: '2026-01-25' });
    });
  });
});
