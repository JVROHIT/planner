import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { useLogin } from '../useLogin';
import { api } from '@/lib/api';
import * as storage from '@/lib/auth/storage';

// Mock next/navigation
const mockPush = vi.fn();
vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: mockPush }),
}));

// Mock API
vi.mock('@/lib/api', () => ({
  api: {
    post: vi.fn(),
  },
  ApiError: class ApiError extends Error {
    status: number;
    errorCode: string | null;
    constructor(message: string, status: number, errorCode: string | null = null) {
      super(message);
      this.status = status;
      this.errorCode = errorCode;
    }
  },
}));

// Mock storage
vi.mock('@/lib/auth/storage', () => ({
  storeAuth: vi.fn(),
}));

// Create wrapper with QueryClient
const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });
  const Wrapper = ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
  Wrapper.displayName = 'QueryClientWrapper';
  return Wrapper;
};

describe('useLogin', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls POST /api/auth/login with credentials', async () => {
    const mockResponse = { token: 'jwt-token', userId: 'user-123' };
    vi.mocked(api.post).mockResolvedValueOnce(mockResponse);

    const { result } = renderHook(() => useLogin(), { wrapper: createWrapper() });

    await result.current.login({ email: 'test@example.com', password: 'password' });

    expect(api.post).toHaveBeenCalledWith('/api/auth/login', {
      email: 'test@example.com',
      password: 'password',
    });
  });

  it('stores token on success', async () => {
    const mockResponse = { token: 'jwt-token', userId: 'user-123' };
    vi.mocked(api.post).mockResolvedValueOnce(mockResponse);

    const { result } = renderHook(() => useLogin(), { wrapper: createWrapper() });

    await result.current.login({ email: 'test@example.com', password: 'password' });

    expect(storage.storeAuth).toHaveBeenCalledWith('jwt-token', 'user-123');
  });

  it('redirects to /today on success', async () => {
    const mockResponse = { token: 'jwt-token', userId: 'user-123' };
    vi.mocked(api.post).mockResolvedValueOnce(mockResponse);

    const { result } = renderHook(() => useLogin(), { wrapper: createWrapper() });

    await result.current.login({ email: 'test@example.com', password: 'password' });

    expect(mockPush).toHaveBeenCalledWith('/today');
  });

  it('returns error on 401', async () => {
    const { ApiError } = await import('@/lib/api');
    vi.mocked(api.post).mockRejectedValueOnce(
      new ApiError('Invalid credentials', 401, 'UNAUTHORIZED')
    );

    const { result } = renderHook(() => useLogin(), { wrapper: createWrapper() });

    try {
      await result.current.login({ email: 'test@example.com', password: 'wrong' });
    } catch {
      // Expected to throw
    }

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });
  });
});
