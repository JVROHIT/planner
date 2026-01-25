'use client';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode, useState } from 'react';

/**
 * Default query client options for FocusFlow.
 *
 * Configuration:
 * - staleTime: 30 seconds - data is considered fresh for 30s
 * - refetchOnWindowFocus: false - don't refetch on tab focus
 * - retry: 1 - retry failed requests once
 */
function createQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 30000, // 30 seconds
        refetchOnWindowFocus: false,
        retry: 1,
        refetchOnReconnect: true,
      },
      mutations: {
        retry: 0,
      },
    },
  });
}

interface QueryProviderProps {
  children: ReactNode;
}

/**
 * TanStack Query provider for the application.
 *
 * Provides:
 * - Query caching with 30s stale time
 * - Automatic error boundaries
 * - Query invalidation support
 *
 * @example
 * // In layout.tsx
 * <QueryProvider>
 *   {children}
 * </QueryProvider>
 */
export function QueryProvider({ children }: QueryProviderProps) {
  // Create query client once per component instance
  // Using useState ensures client persists across renders
  const [queryClient] = useState(() => createQueryClient());

  return (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  );
}

export default QueryProvider;
