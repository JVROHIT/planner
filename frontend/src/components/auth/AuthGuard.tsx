'use client';

import { ReactNode, useEffect } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';

interface AuthGuardProps {
    children: ReactNode;
}

const PUBLIC_PATHS = ['/login', '/signup'];

/**
 * Guard component that redirects to login if user is not authenticated.
 * 
 * Logic:
 * 1. If path is public, always allow.
 * 2. If path is protected:
 *    - If loading, show nothing (or loading state).
 *    - If not authenticated, redirect to /login.
 *    - If authenticated, allow children.
 */
export function AuthGuard({ children }: AuthGuardProps) {
    const { isAuthenticated, isLoading } = useAuth();
    const router = useRouter();
    const pathname = usePathname();

    const isPublicPath = PUBLIC_PATHS.includes(pathname);

    useEffect(() => {
        if (!isLoading) {
            if (!isAuthenticated && !isPublicPath) {
                router.push('/login');
            } else if (isAuthenticated && isPublicPath) {
                router.push('/');
            }
        }
    }, [isAuthenticated, isLoading, isPublicPath, router]);

    // Always show public paths
    if (isPublicPath) {
        return <>{children}</>;
    }

    // Show nothing while loading auth state to prevent flash of protected content
    if (isLoading) {
        return null;
    }

    // Only show children if authenticated
    if (isAuthenticated) {
        return <>{children}</>;
    }

    // Fallback (will redirect via useEffect)
    return null;
}
