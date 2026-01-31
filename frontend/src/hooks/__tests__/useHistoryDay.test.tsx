import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { useHistoryDay } from '../useHistoryDay';
import { api } from '@/lib/api';
import type { DailyPlan } from '@/types/domain';

// Mock API
vi.mock('@/lib/api', () => ({
    api: {
        get: vi.fn(),
    },
    ApiError: class ApiError extends Error {
        status: number;
        errorCode: string | null;
        constructor(message: string, status: number, errorCode: string | null = null) {
            super(message);
            this.status = status;
            this.errorCode = errorCode;
        }
    },
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

describe('useHistoryDay', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('fetches from /api/daily/{date}', async () => {
        const date = '2026-01-20';
        const mockPlan: DailyPlan = {
            id: 'plan-1',
            userId: 'user-123',
            day: date,
            entries: [],
            closed: true,
        };
        vi.mocked(api.get).mockResolvedValueOnce(mockPlan);

        const { result } = renderHook(() => useHistoryDay(date), { wrapper: createWrapper() });

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        expect(api.get).toHaveBeenCalledWith(`/api/daily/${date}`);
        expect(result.current.data).toEqual(mockPlan);
    });

    it('is read-only (no mutation functions exposed)', () => {
        const { result } = renderHook(() => useHistoryDay('2026-01-20'), { wrapper: createWrapper() });

        // Result from useQuery should not have mutate/mutateAsync
        const hookResult = result.current as unknown as Record<string, unknown>;
        expect(hookResult.mutate).toBeUndefined();
        expect(hookResult.mutateAsync).toBeUndefined();
    });
});
