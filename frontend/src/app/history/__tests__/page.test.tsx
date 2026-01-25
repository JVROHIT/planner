import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import HistoryPage from '../page';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useRecentHistory } from '@/hooks';

// Mock AppShell
vi.mock('@/components/layout', () => ({
    AppShell: ({ children }: { children: React.ReactNode }) => <div data-testid="app-shell">{children}</div>,
}));

// Mock hooks
vi.mock('@/hooks', () => ({
    useRecentHistory: vi.fn(),
}));

// Mock next/navigation
vi.mock('next/navigation', () => ({
    useRouter: () => ({
        push: vi.fn(),
    }),
}));

const queryClient = new QueryClient();
const Wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
);

describe('HistoryPage Index', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders summary and loading state', () => {
        vi.mocked(useRecentHistory).mockReturnValue({
            data: undefined,
            isLoading: true,
            error: null,
        } as unknown as ReturnType<typeof useRecentHistory>);

        render(<HistoryPage />, { wrapper: Wrapper });

        expect(screen.getByText('Activity History')).toBeDefined();
    });

    it('renders recent events when loaded', async () => {
        const mockEvents = [
            {
                id: 'e1',
                type: 'TASK_COMPLETED',
                payload: { description: 'Test Task' },
                occurredAt: '2026-01-25T10:00:00Z',
            },
        ];
        vi.mocked(useRecentHistory).mockReturnValue({
            data: mockEvents,
            isLoading: false,
            error: null,
        } as unknown as unknown as any);

        render(<HistoryPage />, { wrapper: Wrapper });

        await waitFor(() => {
            expect(screen.getByText('Task completed: "Test Task"')).toBeDefined();
        });
    });

    it('shows empty state when no events exist', async () => {
        vi.mocked(useRecentHistory).mockReturnValue({
            data: [],
            isLoading: false,
            error: null,
        } as unknown as unknown as any);

        render(<HistoryPage />, { wrapper: Wrapper });

        await waitFor(() => {
            expect(screen.getByText('No recent events found.')).toBeDefined();
        });
    });
});
