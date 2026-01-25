import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { DatePicker } from '../DatePicker';
import { format, subDays, startOfToday } from 'date-fns';

describe('DatePicker', () => {
    const todayStr = format(startOfToday(), 'yyyy-MM-dd');

    it('renders correctly with selected date', () => {
        const selectedDate = '2026-01-20';
        render(<DatePicker selectedDate={selectedDate} onDateChange={() => { }} />);

        expect(screen.getByText('Tuesday')).toBeDefined();
        expect(screen.getByText('January 20th, 2026')).toBeDefined();
    });

    it('calls onDateChange when previous button is clicked', () => {
        const selectedDate = '2026-01-20';
        const onDateChange = vi.fn();
        render(<DatePicker selectedDate={selectedDate} onDateChange={onDateChange} />);

        const prevButton = screen.getByLabelText('Previous Day');
        fireEvent.click(prevButton);

        expect(onDateChange).toHaveBeenCalledWith('2026-01-19');
    });

    it('disables next button for today', () => {
        const onDateChange = vi.fn();
        render(<DatePicker selectedDate={todayStr} onDateChange={onDateChange} />);

        const nextButton = screen.getByLabelText('Next Day');
        expect((nextButton as HTMLButtonElement).disabled).toBe(true);

        fireEvent.click(nextButton);
        expect(onDateChange).not.toHaveBeenCalled();
    });

    it('enables next button for past date', () => {
        const yesterdayStr = format(subDays(startOfToday(), 1), 'yyyy-MM-dd');
        const onDateChange = vi.fn();
        render(<DatePicker selectedDate={yesterdayStr} onDateChange={onDateChange} />);

        const nextButton = screen.getByLabelText('Next Day');
        expect((nextButton as HTMLButtonElement).disabled).toBe(false);

        fireEvent.click(nextButton);
        expect(onDateChange).toHaveBeenCalledWith(todayStr);
    });
});
