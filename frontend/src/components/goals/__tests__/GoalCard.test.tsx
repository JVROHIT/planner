import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { GoalCard } from '../GoalCard';
import type { GoalDetail } from '@/types/domain';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// Mock KeyResultList because it uses hooks
vi.mock('../KeyResultList', () => ({
    KeyResultList: () => <div data-testid="key-result-list" />
}));

const mockGoalDetail: GoalDetail = {
    goal: {
        id: 'goal-1',
        userId: 'user-123',
        title: 'Test Goal',
        horizon: 'MONTH',
        startDate: '2026-01-01',
        endDate: '2026-02-01',
        status: 'ACTIVE',
    },
    keyResults: [],
    latestSnapshot: null,
    status: 'AHEAD',
    trend: 'UP',
    actualPercent: 75,
    expectedPercent: 50,
};

const queryClient = new QueryClient();

describe('GoalCard', () => {
    it('renders goal title', () => {
        render(
            <QueryClientProvider client={queryClient}>
                <GoalCard goalDetail={mockGoalDetail} />
            </QueryClientProvider>
        );

        expect(screen.getByText('Test Goal')).toBeDefined();
    });

    it('displays actual vs expected percentage', () => {
        render(
            <QueryClientProvider client={queryClient}>
                <GoalCard goalDetail={mockGoalDetail} />
            </QueryClientProvider>
        );

        expect(screen.getByText('75.0%')).toBeDefined();
        expect(screen.getByText('50.0%')).toBeDefined();
    });

    it('shows status badge', () => {
        render(
            <QueryClientProvider client={queryClient}>
                <GoalCard goalDetail={mockGoalDetail} />
            </QueryClientProvider>
        );

        expect(screen.getByText('AHEAD')).toBeDefined();
    });

    it('displays trend indicator', () => {
        render(
            <QueryClientProvider client={queryClient}>
                <GoalCard goalDetail={mockGoalDetail} />
            </QueryClientProvider>
        );

        expect(screen.getByText('↑')).toBeDefined();
    });

    it('expands to show key results when clicked', () => {
        render(
            <QueryClientProvider client={queryClient}>
                <GoalCard goalDetail={mockGoalDetail} />
            </QueryClientProvider>
        );

        const button = screen.getByText('Show Key Results');
        fireEvent.click(button);

        expect(screen.getByTestId('key-result-list')).toBeDefined();
        expect(screen.getByText('Hide Key Results')).toBeDefined();
    });

    it('never computes progress client-side (invariant)', () => {
        // This is more of a code audit invariant, but we verify it renders what's passed
        render(
            <QueryClientProvider client={queryClient}>
                <GoalCard goalDetail={mockGoalDetail} />
            </QueryClientProvider>
        );

        // If actualPercent was 75, it should show 75.0%
        expect(screen.getByText('75.0%')).toBeDefined();
    });
});
