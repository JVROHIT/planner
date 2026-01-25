'use client';

import { useMutation } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { api, ApiError } from '@/lib/api';
import { storeAuth } from '@/lib/auth/storage';
import type { AuthRequest, AuthResponse } from '@/types/domain';

interface UseLoginOptions {
  onSuccess?: () => void;
  onError?: (error: ApiError) => void;
}

/**
 * Hook for user login.
 *
 * Makes POST request to /api/auth/login.
 * On success, stores token and redirects to /today.
 *
 * @example
 * const { login, isLoading, error } = useLogin();
 *
 * await login({ email: 'user@example.com', password: 'secret' });
 */
export function useLogin(options?: UseLoginOptions) {
  const router = useRouter();

  const mutation = useMutation({
    mutationFn: async (credentials: AuthRequest): Promise<AuthResponse> => {
      return api.post<AuthResponse>('/api/auth/login', credentials);
    },
    onSuccess: (data) => {
      // Store authentication data
      storeAuth(data.token, data.userId);

      // Call custom success handler if provided
      options?.onSuccess?.();

      // Redirect to today page
      router.push('/today');
    },
    onError: (error: Error) => {
      if (error instanceof ApiError) {
        options?.onError?.(error);
      }
    },
  });

  return {
    login: mutation.mutateAsync,
    isLoading: mutation.isPending,
    error: mutation.error as ApiError | null,
    isError: mutation.isError,
    reset: mutation.reset,
  };
}

export default useLogin;
