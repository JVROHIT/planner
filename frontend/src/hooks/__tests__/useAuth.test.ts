import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useAuth } from '../useAuth';
import * as storage from '@/lib/auth/storage';

// Mock next/navigation
const mockPush = vi.fn();
vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: mockPush }),
}));

// Mock storage module
vi.mock('@/lib/auth/storage', () => ({
  getToken: vi.fn(),
  getUserId: vi.fn(),
  clearAuth: vi.fn(),
  isAuthenticated: vi.fn(),
}));

describe('useAuth', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('returns isAuthenticated: false when no token', () => {
    vi.mocked(storage.getToken).mockReturnValue(null);
    vi.mocked(storage.getUserId).mockReturnValue(null);

    const { result } = renderHook(() => useAuth());

    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBeNull();
  });

  it('returns isAuthenticated: true when valid token exists', () => {
    vi.mocked(storage.getToken).mockReturnValue('valid-token');
    vi.mocked(storage.getUserId).mockReturnValue('user-123');

    const { result } = renderHook(() => useAuth());

    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.user).toEqual({ id: 'user-123' });
  });

  it('logout clears token and redirects', () => {
    vi.mocked(storage.getToken).mockReturnValue('valid-token');
    vi.mocked(storage.getUserId).mockReturnValue('user-123');

    const { result } = renderHook(() => useAuth());

    act(() => {
      result.current.logout();
    });

    expect(storage.clearAuth).toHaveBeenCalled();
    expect(mockPush).toHaveBeenCalledWith('/login');
  });

  it('isLoading becomes false after checking auth', () => {
    vi.mocked(storage.getToken).mockReturnValue(null);

    const { result } = renderHook(() => useAuth());

    // isLoading should be false after the effect runs
    expect(result.current.isLoading).toBe(false);
  });
});
