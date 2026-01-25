import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { AddTaskDialog } from '../AddTaskDialog';
import type { Task, WeeklyPlan } from '@/types/domain';

// Mock hooks
const mockUseCreateTask = vi.fn();
const mockUseUpdateWeeklyPlan = vi.fn();
const mockUseWeeklyPlan = vi.fn();

vi.mock('@/hooks', () => ({
  useCreateTask: () => mockUseCreateTask(),
  useUpdateWeeklyPlan: () => mockUseUpdateWeeklyPlan(),
  useWeeklyPlan: () => mockUseWeeklyPlan(),
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

describe('AddTaskDialog', () => {
  const mockWeeklyPlan: WeeklyPlan = {
    id: 'plan-1',
    userId: 'user-123',
    weekNumber: 4,
    year: 2026,
    weekStartDate: '2026-01-20',
    taskGrid: {
      MONDAY: [],
      TUESDAY: [],
      WEDNESDAY: [],
      THURSDAY: [],
      FRIDAY: [],
      SATURDAY: [],
      SUNDAY: [],
    },
    createdAt: '2026-01-20T00:00:00Z',
    updatedAt: '2026-01-20T00:00:00Z',
  };

  const mockCreatedTask: Task = {
    id: 'task-123',
    description: 'New task',
    userId: 'user-123',
    createdAt: '2026-01-25T00:00:00Z',
    updatedAt: '2026-01-25T00:00:00Z',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseCreateTask.mockReturnValue({
      mutateAsync: vi.fn().mockResolvedValue(mockCreatedTask),
    });
    mockUseUpdateWeeklyPlan.mockReturnValue({
      mutateAsync: vi.fn().mockResolvedValue(mockWeeklyPlan),
    });
    mockUseWeeklyPlan.mockReturnValue({
      data: mockWeeklyPlan,
      isLoading: false,
    });
  });

  it('opens on add button click', () => {
    const Wrapper = createWrapper();
    render(
      <AddTaskDialog
        isOpen={true}
        onClose={vi.fn()}
        day="2026-01-25"
        weekStart="2026-01-20"
      />,
      { wrapper: Wrapper }
    );

    expect(screen.getByRole('heading', { name: 'Add Task' })).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/e.g., Attend team standup/i)).toBeInTheDocument();
  });

  it('does not render when closed', () => {
    const Wrapper = createWrapper();
    render(
      <AddTaskDialog
        isOpen={false}
        onClose={vi.fn()}
        day="2026-01-25"
        weekStart="2026-01-20"
      />,
      { wrapper: Wrapper }
    );

    expect(screen.queryByText('Add Task')).not.toBeInTheDocument();
  });

  it('creates task on submit', async () => {
    const user = userEvent.setup();
    const mockCreateTask = vi.fn().mockResolvedValue(mockCreatedTask);
    const mockUpdatePlan = vi.fn().mockResolvedValue(mockWeeklyPlan);
    mockUseCreateTask.mockReturnValue({
      mutateAsync: mockCreateTask,
    });
    mockUseUpdateWeeklyPlan.mockReturnValue({
      mutateAsync: mockUpdatePlan,
    });

    const onClose = vi.fn();
    const Wrapper = createWrapper();
    render(
      <AddTaskDialog
        isOpen={true}
        onClose={onClose}
        day="2026-01-25"
        weekStart="2026-01-20"
      />,
      { wrapper: Wrapper }
    );

    const input = screen.getByPlaceholderText(/e.g., Attend team standup/i);
    await user.type(input, 'New task');

    const submitButton = screen.getByRole('button', { name: /add task/i });
    await user.click(submitButton);

    await vi.waitFor(() => {
      expect(mockCreateTask).toHaveBeenCalledWith({ description: 'New task' });
      expect(mockUpdatePlan).toHaveBeenCalled();
      expect(onClose).toHaveBeenCalled();
    });
  });

  it('validates description is required', async () => {
    const user = userEvent.setup();
    const Wrapper = createWrapper();
    render(
      <AddTaskDialog
        isOpen={true}
        onClose={vi.fn()}
        day="2026-01-25"
        weekStart="2026-01-20"
      />,
      { wrapper: Wrapper }
    );

    const submitButton = screen.getByRole('button', { name: /add task/i });
    expect(submitButton).toBeDisabled();

    const input = screen.getByPlaceholderText(/e.g., Attend team standup/i);
    await user.type(input, '   '); // Only whitespace
    expect(submitButton).toBeDisabled();

    await user.clear(input);
    await user.type(input, 'Valid task');
    expect(submitButton).not.toBeDisabled();
  });

  it('calls onClose when cancel is clicked', async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();
    const Wrapper = createWrapper();
    render(
      <AddTaskDialog
        isOpen={true}
        onClose={onClose}
        day="2026-01-25"
        weekStart="2026-01-20"
      />,
      { wrapper: Wrapper }
    );

    const cancelButton = screen.getByRole('button', { name: /cancel/i });
    await user.click(cancelButton);

    expect(onClose).toHaveBeenCalled();
  });
});
