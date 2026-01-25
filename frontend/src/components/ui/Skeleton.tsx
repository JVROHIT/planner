'use client';

import { cn } from '@/lib/utils';

/**
 * Skeleton Loader Component
 * 
 * Provides a placeholder for content while it's loading.
 * 
 * @param className CSS classes to control shape and size
 */
export function Skeleton({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
    return (
        <div
            className={cn('animate-pulse rounded-md bg-muted', className)}
            {...props}
        />
    );
}

export default Skeleton;
