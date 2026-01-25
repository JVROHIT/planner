import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { useRecentHistory } from '../useRecentHistory';
import { api } from '@/lib/api';
import type { AuditEvent } from '@/types/domain';

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

describe('useRecentHistory', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('fetches from /api/history/recent?limit={n}', async () => {
        const limit = 10;
        const mockEvents: AuditEvent[] = [
            {
                id: 'event-1',
                userId: 'user-123',
                type: 'TASK_COMPLETED',
                payload: { taskId: 'task-1' },
                occurredAt: '2026-01-25T10:00:00Z',
            },
        ];
        vi.mocked(api.get).mockResolvedValueOnce(mockEvents);

        const { result } = renderHook(() => useRecentHistory(limit), { wrapper: createWrapper() });

        await waitFor(() => {
            expect(result.current.isSuccess).toBe(true);
        });

        expect(api.get).toHaveBeenCalledWith(`/api/history/recent?limit=${limit}`);
        expect(result.current.data).toEqual(mockEvents);
    });

    it('is read-only', () => {
        const { result } = renderHook(() => useRecentHistory(), { wrapper: createWrapper() });
        const hookResult = result.current as unknown as Record<string, unknown>;
        expect(hookResult.mutate).toBeUndefined();
    });
});
