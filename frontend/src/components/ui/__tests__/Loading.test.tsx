import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Skeleton } from '../Skeleton';
import { LoadingSpinner } from '../LoadingSpinner';

describe('UI Loading Components', () => {
    describe('Skeleton', () => {
        it('renders with pulse animation class', () => {
            const { container } = render(<Skeleton className="w-10 h-10" />);
            expect(container.firstChild).toHaveClass('animate-pulse');
            expect(container.firstChild).toHaveClass('bg-muted');
        });
    });

    describe('LoadingSpinner', () => {
        it('renders with spin animation', () => {
            render(<LoadingSpinner />);
            const spinner = screen.getByRole('status');
            expect(spinner).toHaveClass('animate-spin');
        });

        it('contains screen reader text', () => {
            render(<LoadingSpinner />);
            expect(screen.getByText('Loading...')).toBeDefined();
        });

        it('applies correct size classes', () => {
            const { rerender } = render(<LoadingSpinner size="sm" />);
            expect(screen.getByRole('status')).toHaveClass('w-4');

            rerender(<LoadingSpinner size="lg" />);
            expect(screen.getByRole('status')).toHaveClass('w-12');
        });
    });
});
