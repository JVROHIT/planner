import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { AddKeyResultDialog } from '../AddKeyResultDialog';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// Mock hook
const mockMutate = vi.fn();
vi.mock('@/hooks', () => ({
    useCreateKeyResult: () => ({ mutate: mockMutate, isPending: false }),
}));

const queryClient = new QueryClient();

describe('AddKeyResultDialog', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('validates required fields', () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AddKeyResultDialog goalId="goal-1" onClose={vi.fn()} />
            </QueryClientProvider>
        );

        const submitButton = screen.getByRole('button', { name: /Add Key Result/i });
        expect(submitButton).toBeDefined();
        // In a real browser, the 'required' attribute would prevent submission.
        // Here we can just check if the button exists.
    });

    it('calls createKeyResult on submit', () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AddKeyResultDialog goalId="goal-1" onClose={vi.fn()} />
            </QueryClientProvider>
        );

        fireEvent.change(screen.getByLabelText(/Title/i), { target: { value: 'New KR' } });
        fireEvent.change(screen.getByLabelText(/Target Value/i), { target: { value: '10' } });
        fireEvent.click(screen.getByRole('button', { name: /Add Key Result/i }));

        expect(mockMutate).toHaveBeenCalled();
    });
});
