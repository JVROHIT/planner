'use client';

import { useParams, useRouter } from 'next/navigation';
import { AppShell } from '@/components/layout';
import { DatePicker } from '@/components/history/DatePicker';
import { HistoryDayView } from '@/components/history/HistoryDayView';
import { useHistoryDay } from '@/hooks';
import { isValid, parseISO } from 'date-fns';
import { ApiError } from '@/components/error/ApiError';
import { Skeleton } from '@/components/ui/Skeleton';

/**
 * History Day Page
 * 
 * Displays the truth mode view for a specific date.
 * Read-only, frozen visuals.
 */
export default function HistoryDayPage() {
    const params = useParams();
    const router = useRouter();
    const dateStr = params.date as string;

    // Validate date format
    const date = parseISO(dateStr);
    const isDateValid = isValid(date);

    const { data: plan, isLoading, error, refetch } = useHistoryDay(dateStr);

    const handleDateChange = (newDate: string) => {
        router.push(`/history/${newDate}`);
    };

    if (!isDateValid) {
        return (
            <AppShell>
                <div className="max-w-4xl mx-auto p-8 text-center">
                    <h1 className="text-2xl font-bold text-destructive">Invalid Date</h1>
                    <p className="mt-2 text-muted-foreground">The date format is incorrect.</p>
                    <button
                        onClick={() => router.push('/history')}
                        className="mt-4 px-4 py-2 bg-primary text-primary-foreground rounded-lg"
                    >
                        Go to History
                    </button>
                </div>
            </AppShell>
        );
    }

    return (
        <AppShell>
            <div className="max-w-4xl mx-auto space-y-8">
                <header className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                    <div>
                        <h1 className="text-3xl font-bold tracking-tight">Truth mode</h1>
                        <p className="text-muted-foreground">Viewing the immutable record of the past.</p>
                    </div>
                    <DatePicker selectedDate={dateStr} onDateChange={handleDateChange} />
                </header>

                <section className="bg-card border rounded-xl p-6 shadow-sm">
                    {isLoading ? (
                        <div className="space-y-4">
                            <Skeleton className="h-8 w-1/4" />
                            <div className="space-y-2">
                                <Skeleton className="h-12 w-full" />
                                <Skeleton className="h-12 w-5/6" />
                                <Skeleton className="h-12 w-full" />
                            </div>
                        </div>
                    ) : error ? (
                        <ApiError error={error} reset={refetch} />
                    ) : plan ? (
                        <HistoryDayView plan={plan} />
                    ) : (
                        <div className="text-center py-12">
                            <p className="text-muted-foreground">No data found for this date.</p>
                        </div>
                    )}
                </section>
            </div>
        </AppShell>
    );
}
