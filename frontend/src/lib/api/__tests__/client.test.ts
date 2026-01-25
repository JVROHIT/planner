import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { api, setToken, clearToken, hasToken } from '../client';
import { ApiError } from '../types';

// Mock fetch globally
const mockFetch = vi.fn();
global.fetch = mockFetch;

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => { store[key] = value; },
    removeItem: (key: string) => { delete store[key]; },
    clear: () => { store = {}; },
  };
})();

Object.defineProperty(window, 'localStorage', { value: localStorageMock });

describe('API Client', () => {
  beforeEach(() => {
    mockFetch.mockReset();
    localStorageMock.clear();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('Token management', () => {
    it('setToken stores token in localStorage', () => {
      setToken('test-token');
      expect(localStorage.getItem('focusflow_token')).toBe('test-token');
    });

    it('clearToken removes token from localStorage', () => {
      setToken('test-token');
      clearToken();
      expect(localStorage.getItem('focusflow_token')).toBeNull();
    });

    it('hasToken returns true when token exists', () => {
      setToken('test-token');
      expect(hasToken()).toBe(true);
    });

    it('hasToken returns false when no token', () => {
      expect(hasToken()).toBe(false);
    });
  });

  describe('api.get', () => {
    it('extracts data from successful ApiResponse', async () => {
      const mockData = { id: '123', name: 'Test' };
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          success: true,
          data: mockData,
          errorCode: null,
          message: null,
        }),
      });

      const result = await api.get<typeof mockData>('/api/test');
      expect(result).toEqual(mockData);
    });

    it('throws ApiError on success: false', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({
          success: false,
          data: null,
          errorCode: 'VALIDATION_ERROR',
          message: 'Invalid input',
        }),
      });

      try {
        await api.get('/api/test');
        expect.fail('Should have thrown an error');
      } catch (error) {
        expect(error).toBeInstanceOf(ApiError);
        expect((error as ApiError).message).toBe('Invalid input');
        expect((error as ApiError).status).toBe(400);
        expect((error as ApiError).errorCode).toBe('VALIDATION_ERROR');
      }
    });

    it('includes authorization header when token exists', async () => {
      setToken('my-jwt-token');
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: {}, errorCode: null, message: null }),
      });

      await api.get('/api/test');

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.any(Headers),
        })
      );

      const headers = mockFetch.mock.calls[0][1].headers as Headers;
      expect(headers.get('Authorization')).toBe('Bearer my-jwt-token');
    });

    it('constructs correct URLs from base URL', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: {}, errorCode: null, message: null }),
      });

      await api.get('/api/dashboard/today');

      expect(mockFetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/dashboard/today',
        expect.any(Object)
      );
    });
  });

  describe('api.post', () => {
    it('sends body as JSON', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: { id: '1' }, errorCode: null, message: null }),
      });

      await api.post('/api/test', { name: 'Test' });

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ name: 'Test' }),
        })
      );
    });
  });

  describe('api.put', () => {
    it('sends PUT request with body', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: {}, errorCode: null, message: null }),
      });

      await api.put('/api/test/1', { name: 'Updated' });

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify({ name: 'Updated' }),
        })
      );
    });
  });

  describe('api.delete', () => {
    it('sends DELETE request', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: null, errorCode: null, message: null }),
      });

      await api.delete('/api/test/1');

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          method: 'DELETE',
        })
      );
    });
  });
});
