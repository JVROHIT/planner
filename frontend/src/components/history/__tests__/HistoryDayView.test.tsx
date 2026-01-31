import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { HistoryDayView } from '../HistoryDayView';
import type { DailyPlan } from '@/types/domain';

const mockPlan: DailyPlan = {
    id: 'plan-1',
    userId: 'user-123',
    day: '2026-01-20',
    closed: true,
    entries: [
        {
            taskId: 'task-1',
            title: 'Completed Task',
            status: 'COMPLETED',
        },
        {
            taskId: 'task-2',
            title: 'Missed Task',
            status: 'MISSED',
        },
        {
            taskId: 'task-3',
            title: 'Pending Task',
            status: 'PENDING',
        }
    ],
};

describe('HistoryDayView', () => {
    it('renders all tasks from the plan', () => {
        render(<HistoryDayView plan={mockPlan} />);

        expect(screen.getByText('Completed Task')).toBeDefined();
        expect(screen.getByText('Missed Task')).toBeDefined();
        expect(screen.getByText('Pending Task')).toBeDefined();
    });

    it('displays FROZEN status', () => {
        render(<HistoryDayView plan={mockPlan} />);
        expect(screen.getByText('FROZEN')).toBeDefined();
        expect(screen.getAllByText('ARCHIVED')).toHaveLength(3);
    });

    it('renders no action buttons (read-only invariant)', () => {
        render(<HistoryDayView plan={mockPlan} />);

        // There should be no "Complete" or "Miss" or "Delete" buttons
        expect(screen.queryByRole('button', { name: /Complete/i })).toBeNull();
        expect(screen.queryByRole('button', { name: /Miss/i })).toBeNull();
        expect(screen.queryByRole('button', { name: /Delete/i })).toBeNull();
    });

    it('shows empty state', () => {
        const emptyPlan = { ...mockPlan, entries: [] };
        render(<HistoryDayView plan={emptyPlan} />);
        expect(screen.getByText('No tasks recorded for this day.')).toBeDefined();
    });
});
