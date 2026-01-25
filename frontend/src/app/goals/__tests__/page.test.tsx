import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import GoalsPage from '../page';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useGoalsDashboard } from '@/hooks';

// Mock Header to avoid finding "Goals Mode" text in header
vi.mock('@/components/layout/Header', () => ({
    Header: () => <div data-testid="mock-header" />
}));

// Mock AppShell to avoid layout complexity
vi.mock('@/components/layout/AppShell', () => ({
    AppShell: ({ children }: { children: React.ReactNode }) => <div data-testid="app-shell">{children}</div>,
}));

// Mock hooks
vi.mock('@/hooks', () => ({
    useGoalsDashboard: vi.fn(),
    useCreateGoal: () => ({ mutate: vi.fn(), isPending: false }),
}));

const queryClient = new QueryClient();

const Wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
);

describe('GoalsPage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders summary and loading state initially', () => {
        vi.mocked(useGoalsDashboard).mockReturnValue({
            data: undefined,
            isLoading: true,
            error: null,
            refetch: vi.fn(),
        } as unknown as any);

        const { container } = render(<GoalsPage />, { wrapper: Wrapper });

        expect(screen.getByRole('heading', { level: 1, name: 'Goals' })).toBeDefined();
        expect(container.querySelectorAll('.animate-pulse').length).toBeGreaterThan(0);
    });

    it('renders list of goals when data is loaded', async () => {
        const mockDashboard = {
            goals: [
                {
                    goal: { id: 'g1', title: 'Goal 1', active: true },
                    keyResults: [],
                    status: 'ON_TRACK',
                    trend: 'FLAT',
                    actualPercent: 50,
                    expectedPercent: 50,
                },
            ],
        };
        vi.mocked(useGoalsDashboard).mockReturnValue({
            data: mockDashboard,
            isLoading: false,
            error: null,
            refetch: vi.fn(),
        } as unknown as any);

        render(<GoalsPage />, { wrapper: Wrapper });

        await waitFor(() => {
            expect(screen.getByText('Goal 1')).toBeDefined();
        });
    });

    it('shows empty state when no goals exist', async () => {
        vi.mocked(useGoalsDashboard).mockReturnValue({
            data: { goals: [] },
            isLoading: false,
            error: null,
            refetch: vi.fn(),
        } as unknown as any);

        render(<GoalsPage />, { wrapper: Wrapper });

        await waitFor(() => {
            expect(screen.getByText('No goals yet')).toBeDefined();
        });
    });
});
