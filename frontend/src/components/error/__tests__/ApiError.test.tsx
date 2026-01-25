import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ApiError } from '../ApiError';
import { ApiError as ApiLibraryError } from '@/lib/api';

describe('ApiError', () => {
    it('renders title and message correctly', () => {
        const error = new ApiLibraryError('Not found', 404, 'NOT_FOUND');
        render(<ApiError error={error} />);

        expect(screen.getByText('Not Found')).toBeDefined();
        expect(screen.getByText(/could not be found/i)).toBeDefined();
    });

    it('renders reset button when provided', () => {
        const reset = vi.fn();
        render(<ApiError error={new Error()} reset={reset} />);

        const button = screen.getByText('Try Again');
        fireEvent.click(button);
        expect(reset).toHaveBeenCalled();
    });

    it('handles generic Error objects', () => {
        const msg = 'Something broke';
        render(<ApiError error={new Error(msg)} />);

        expect(screen.getByText(msg)).toBeDefined();
    });
});
