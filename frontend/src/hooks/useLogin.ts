'use client';

import { useMutation } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { api, ApiError } from '@/lib/api';
import { useAuth } from './useAuth';
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
  const { login } = useAuth();

  const mutation = useMutation({
    mutationFn: async (credentials: AuthRequest): Promise<AuthResponse> => {
      return api.post<AuthResponse>('/api/auth/login', credentials);
    },
    onSuccess: (data) => {
      // Use centralized login to update state and redirect
      login(data.token, data.userId);

      // Call custom success handler if provided
      options?.onSuccess?.();
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
