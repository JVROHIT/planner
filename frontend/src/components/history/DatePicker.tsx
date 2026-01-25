'use client';

import { format, subDays, addDays, isAfter, startOfToday, parseISO } from 'date-fns';

/**
 * DatePicker Component
 * 
 * Allows navigation between dates for the History screen.
 * Prevents selection of future dates.
 * 
 * @param selectedDate Currently selected date (YYYY-MM-DD)
 * @param onDateChange Callback when a new date is selected
 */

interface DatePickerProps {
    selectedDate: string;
    onDateChange: (date: string) => void;
}

export function DatePicker({ selectedDate, onDateChange }: DatePickerProps) {
    const today = startOfToday();
    const currentDate = parseISO(selectedDate);

    const handlePrev = () => {
        const prevDate = subDays(currentDate, 1);
        onDateChange(format(prevDate, 'yyyy-MM-dd'));
    };

    const handleNext = () => {
        const nextDate = addDays(currentDate, 1);
        if (!isAfter(nextDate, today)) {
            onDateChange(format(nextDate, 'yyyy-MM-dd'));
        }
    };

    const isNextDisabled = isAfter(addDays(currentDate, 1), today);

    return (
        <div className="flex items-center gap-4 bg-card border rounded-lg p-2 shadow-sm">
            <button
                onClick={handlePrev}
                className="p-2 hover:bg-muted rounded-md transition-colors"
                aria-label="Previous Day"
            >
                ←
            </button>

            <div className="flex flex-col items-center min-w-[150px]">
                <span className="text-sm font-medium text-muted-foreground">
                    {format(currentDate, 'EEEE')}
                </span>
                <span className="text-lg font-bold">
                    {format(currentDate, 'MMMM do, yyyy')}
                </span>
            </div>

            <button
                onClick={handleNext}
                disabled={isNextDisabled}
                className={`p-2 rounded-md transition-colors ${isNextDisabled
                    ? 'text-muted-foreground cursor-not-allowed opacity-50'
                    : 'hover:bg-muted'
                    }`}
                aria-label="Next Day"
            >
                →
            </button>
        </div>
    );
}

export default DatePicker;
