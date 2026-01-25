import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import HistoryDayPage from '../page';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useHistoryDay } from '@/hooks';
import { useParams } from 'next/navigation';

// Mock AppShell
vi.mock('@/components/layout', () => ({
    AppShell: ({ children }: { children: React.ReactNode }) => <div data-testid="app-shell">{children}</div>,
}));

// Mock hooks
vi.mock('@/hooks', () => ({
    useHistoryDay: vi.fn(),
}));

// Mock next/navigation
vi.mock('next/navigation', () => ({
    useRouter: () => ({
        push: vi.fn(),
    }),
    useParams: vi.fn(),
}));

// Mock components
vi.mock('@/components/history/DatePicker', () => ({
    DatePicker: () => <div data-testid="date-picker" />
}));

vi.mock('@/components/history/HistoryDayView', () => ({
    HistoryDayView: () => <div data-testid="history-day-view" />
}));

const queryClient = new QueryClient();
const Wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
);

describe('HistoryDayPage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders loading state', () => {
        vi.mocked(useParams).mockReturnValue({ date: '2026-01-20' });
        vi.mocked(useHistoryDay).mockReturnValue({
            data: undefined,
            isLoading: true,
            error: null,
        } as unknown as ReturnType<typeof useHistoryDay>);

        render(<HistoryDayPage />, { wrapper: Wrapper });

        expect(screen.getByText('Truth mode')).toBeDefined();
    });

    it('renders history day view when data is loaded', async () => {
        vi.mocked(useParams).mockReturnValue({ date: '2026-01-20' });
        vi.mocked(useHistoryDay).mockReturnValue({
            data: { id: 'p1', day: '2026-01-20', tasks: [], closed: true },
            isLoading: false,
            error: null,
        } as unknown as any);

        render(<HistoryDayPage />, { wrapper: Wrapper });

        await waitFor(() => {
            expect(screen.getByTestId('history-day-view')).toBeDefined();
        });
    });

    it('shows error state when load fails', async () => {
        vi.mocked(useParams).mockReturnValue({ date: '2026-01-20' });
        vi.mocked(useHistoryDay).mockReturnValue({
            data: null,
            isLoading: false,
            error: new Error('Network error'),
        } as unknown as any);

        render(<HistoryDayPage />, { wrapper: Wrapper });

        await waitFor(() => {
            expect(screen.getByText('Failed to load plan')).toBeDefined();
            expect(screen.getByText('Network error')).toBeDefined();
        });
    });

    it('handles invalid date', () => {
        vi.mocked(useParams).mockReturnValue({ date: 'invalid-date' });

        render(<HistoryDayPage />, { wrapper: Wrapper });

        expect(screen.getByText('Invalid Date')).toBeDefined();
    });
});
