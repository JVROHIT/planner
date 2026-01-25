import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ErrorBoundary } from '../ErrorBoundary';

const ThrowError = () => {
    throw new Error('Test error');
};

describe('ErrorBoundary', () => {
    beforeEach(() => {
        // Silence console.error for testing as it's expected
        vi.spyOn(console, 'error').mockImplementation(() => { });
    });

    it('renders children when no error occurs', () => {
        render(
            <ErrorBoundary>
                <div>Safe Content</div>
            </ErrorBoundary>
        );

        expect(screen.getByText('Safe Content')).toBeDefined();
    });

    it('renders fallback UI when error is caught', () => {
        render(
            <ErrorBoundary>
                <ThrowError />
            </ErrorBoundary>
        );

        expect(screen.getByText('Unexpected Error')).toBeDefined();
        expect(screen.getByText('Test error')).toBeDefined();
    });

    it('renders custom fallback when provided', () => {
        render(
            <ErrorBoundary fallback={<div>Custom Fail</div>}>
                <ThrowError />
            </ErrorBoundary>
        );

        expect(screen.getByText('Custom Fail')).toBeDefined();
    });
});
