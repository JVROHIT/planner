/* eslint-disable react-hooks/set-state-in-effect */
'use client';

import { useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getToken, getUserId, clearAuth } from '@/lib/auth/storage';

interface User {
  id: string;
}

interface UseAuthReturn {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  logout: () => void;
}

/**
 * Hook for accessing authentication state.
 *
 * Returns:
 * - user: Current user object (if authenticated)
 * - isAuthenticated: Whether user has valid token
 * - isLoading: Whether auth state is being determined
 * - logout: Function to clear auth and redirect to login
 *
 * @example
 * const { user, isAuthenticated, logout } = useAuth();
 */
function getInitialAuthState() {
  if (typeof window === 'undefined') {
    return { user: null, authenticated: false };
  }
  const token = getToken();
  const userId = getUserId();
  return {
    user: token && userId ? { id: userId } : null,
    authenticated: !!(token && userId),
  };
}

export function useAuth(): UseAuthReturn {
  const router = useRouter();
  const initialState = getInitialAuthState();
  
  const [user, setUser] = useState<User | null>(initialState.user);
  const [isLoading, setIsLoading] = useState(true);
  const [authenticated, setAuthenticated] = useState(initialState.authenticated);

  useEffect(() => {
    // Re-check authentication state on mount (for client-side hydration)
    // This is intentional initialization - we need to sync state from storage
    const currentToken = getToken();
    const currentUserId = getUserId();

    // Intentional: Initialize state from storage on mount
    // This is a one-time initialization pattern required for SSR hydration
    // Disabling ESLint rule as this is necessary for syncing with localStorage
    const newUser = currentToken && currentUserId ? { id: currentUserId } : null;
    const newAuthenticated = !!(currentToken && currentUserId);
    
    // Batch state updates (React 18+ batches automatically)
    setUser(newUser);
    setAuthenticated(newAuthenticated);
    setIsLoading(false);
  }, []);

  const logout = useCallback(() => {
    clearAuth();
    setUser(null);
    setAuthenticated(false);
    router.push('/login');
  }, [router]);

  return {
    user,
    isAuthenticated: authenticated,
    isLoading,
    logout,
  };
}

export default useAuth;
