'use client';

import { cn } from '@/lib/utils';

/**
 * LoadingSpinner Component
 * 
 * A simple animated spinner for loading states.
 * 
 * @param size Size of the spinner ('sm', 'md', 'lg')
 * @param className Additional CSS classes
 */

interface LoadingSpinnerProps {
    size?: 'sm' | 'md' | 'lg';
    className?: string;
}

export function LoadingSpinner({ size = 'md', className }: LoadingSpinnerProps) {
    const sizes = {
        sm: 'w-4 h-4 border-2',
        md: 'w-8 h-8 border-3',
        lg: 'w-12 h-12 border-4',
    };

    return (
        <div
            className={cn(
                'inline-block animate-spin rounded-full border-solid border-current border-r-transparent align-[-0.125em] motion-reduce:animate-[spin_1.5s_linear_infinite] text-primary',
                sizes[size],
                className
            )}
            role="status"
        >
            <span className="sr-only">Loading...</span>
        </div>
    );
}

export default LoadingSpinner;
