'use client';

import { useMutation } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { api, ApiError } from '@/lib/api';
import { useAuth } from './useAuth';
import type { AuthRequest, AuthResponse } from '@/types/domain';

interface UseRegisterOptions {
  onSuccess?: () => void;
  onError?: (error: ApiError) => void;
}

/**
 * Hook for user registration.
 *
 * Makes POST request to /api/auth/register.
 * On success, stores token and redirects to /today.
 *
 * Error codes:
 * - 409 CONFLICT: Email already exists
 * - 400 BAD_REQUEST: Invalid input
 *
 * @example
 * const { register, isLoading, error } = useRegister();
 *
 * await register({ email: 'user@example.com', password: 'secret', weekStart: '2026-02-02' });
 */
export function useRegister(options?: UseRegisterOptions) {
  const { login } = useAuth();

  const mutation = useMutation({
    mutationFn: async (credentials: AuthRequest): Promise<AuthResponse> => {
      return api.post<AuthResponse>('/api/auth/register', credentials);
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
    register: mutation.mutateAsync,
    isLoading: mutation.isPending,
    error: mutation.error as ApiError | null,
    isError: mutation.isError,
    reset: mutation.reset,
  };
}

export default useRegister;
