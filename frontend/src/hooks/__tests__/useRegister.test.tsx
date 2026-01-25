import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { useRegister } from '../useRegister';
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

describe('useRegister', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls POST /api/auth/register', async () => {
    const mockResponse = { token: 'jwt-token', userId: 'user-123' };
    vi.mocked(api.post).mockResolvedValueOnce(mockResponse);

    const { result } = renderHook(() => useRegister(), { wrapper: createWrapper() });

    await result.current.register({ email: 'new@example.com', password: 'password123' });

    expect(api.post).toHaveBeenCalledWith('/api/auth/register', {
      email: 'new@example.com',
      password: 'password123',
    });
  });

  it('stores token and redirects on success', async () => {
    const mockResponse = { token: 'jwt-token', userId: 'user-123' };
    vi.mocked(api.post).mockResolvedValueOnce(mockResponse);

    const { result } = renderHook(() => useRegister(), { wrapper: createWrapper() });

    await result.current.register({ email: 'new@example.com', password: 'password123' });

    expect(storage.storeAuth).toHaveBeenCalledWith('jwt-token', 'user-123');
    expect(mockPush).toHaveBeenCalledWith('/today');
  });

  it('returns error on 409 duplicate email', async () => {
    const { ApiError } = await import('@/lib/api');
    vi.mocked(api.post).mockRejectedValueOnce(
      new ApiError('Email already exists', 409, 'CONFLICT')
    );

    const { result } = renderHook(() => useRegister(), { wrapper: createWrapper() });

    try {
      await result.current.register({ email: 'existing@example.com', password: 'password123' });
    } catch {
      // Expected to throw
    }

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
      expect(result.current.error?.status).toBe(409);
    });
  });
});
