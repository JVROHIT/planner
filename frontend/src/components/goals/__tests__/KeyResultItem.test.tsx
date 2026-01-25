import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { KeyResultItem } from '../KeyResultItem';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { KeyResult } from '@/types/domain';

// Mock hooks
vi.mock('@/hooks', () => ({
    useDeleteKeyResult: () => ({ mutate: vi.fn(), isPending: false }),
    useCompleteMilestone: () => ({ mutate: vi.fn(), isPending: false }),
}));

const mockKeyResult: KeyResult = {
    id: 'kr-1',
    goalId: 'goal-1',
    title: 'Test Key Result',
    type: 'ACCUMULATIVE',
    targetValue: 10,
    currentValue: 5,
    completed: false,
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
};

const queryClient = new QueryClient();

describe('KeyResultItem', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders title and progress', () => {
        render(
            <QueryClientProvider client={queryClient}>
                <KeyResultItem keyResult={mockKeyResult} goalId="goal-1" />
            </QueryClientProvider>
        );

        expect(screen.getByText('Test Key Result')).toBeDefined();
        expect(screen.getByText('5 / 10')).toBeDefined();
    });

    it('shows type indicator', () => {
        render(
            <QueryClientProvider client={queryClient}>
                <KeyResultItem keyResult={mockKeyResult} goalId="goal-1" />
            </QueryClientProvider>
        );

        expect(screen.getByText('Accumulative')).toBeDefined();
    });

    it('shows complete button ONLY for milestones that are not completed', () => {
        const milestoneKR: KeyResult = { ...mockKeyResult, type: 'MILESTONE' };

        const { rerender } = render(
            <QueryClientProvider client={queryClient}>
                <KeyResultItem keyResult={milestoneKR} goalId="goal-1" />
            </QueryClientProvider>
        );

        expect(screen.getByText('Complete')).toBeDefined();

        const completedMilestone: KeyResult = { ...milestoneKR, completed: true };
        rerender(
            <QueryClientProvider client={queryClient}>
                <KeyResultItem keyResult={completedMilestone} goalId="goal-1" />
            </QueryClientProvider>
        );

        expect(screen.queryByText('Complete')).toBeNull();
        expect(screen.getByText('✓ Completed')).toBeDefined();
    });
});
