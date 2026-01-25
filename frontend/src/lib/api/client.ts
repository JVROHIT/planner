import { ApiResponse, ApiError, HttpMethod, RequestOptions } from './types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';
const TOKEN_KEY = 'focusflow_token';

/**
 * Get the stored authentication token.
 */
function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(TOKEN_KEY);
}

/**
 * Store the authentication token.
 */
export function setToken(token: string): void {
  if (typeof window === 'undefined') return;
  localStorage.setItem(TOKEN_KEY, token);
}

/**
 * Remove the authentication token.
 */
export function clearToken(): void {
  if (typeof window === 'undefined') return;
  localStorage.removeItem(TOKEN_KEY);
}

/**
 * Check if user has a stored token.
 */
export function hasToken(): boolean {
  return getToken() !== null;
}

/**
 * Build request headers with optional authorization.
 */
function buildHeaders(customHeaders?: Record<string, string>): Headers {
  const headers = new Headers({
    'Content-Type': 'application/json',
    ...customHeaders,
  });

  const token = getToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  return headers;
}

/**
 * Handle 401 unauthorized by redirecting to login.
 */
function handleUnauthorized(): never {
  clearToken();
  if (typeof window !== 'undefined') {
    window.location.href = '/login';
  }
  throw new ApiError('Unauthorized', 401, 'UNAUTHORIZED');
}

/**
 * Extract data from API response or throw typed error.
 */
async function extractResponse<T>(response: Response): Promise<T> {
  if (response.status === 401) {
    handleUnauthorized();
  }

  let body: ApiResponse<T>;

  try {
    body = await response.json();
  } catch {
    throw new ApiError(
      'Invalid response from server',
      response.status,
      'INVALID_RESPONSE'
    );
  }

  if (!response.ok || !body.success) {
    throw new ApiError(
      body.message ?? 'An error occurred',
      response.status,
      body.errorCode
    );
  }

  if (body.data === null || body.data === undefined) {
    // For void responses, return undefined as T
    return undefined as T;
  }

  return body.data;
}

/**
 * Make an API request.
 */
async function request<T>(
  method: HttpMethod,
  path: string,
  body?: unknown,
  options?: RequestOptions
): Promise<T> {
  const url = `${API_BASE_URL}${path}`;

  const config: RequestInit = {
    method,
    headers: buildHeaders(options?.headers),
    credentials: 'include',
    signal: options?.signal,
  };

  if (body !== undefined && method !== 'GET') {
    config.body = JSON.stringify(body);
  }

  const response = await fetch(url, config);
  return extractResponse<T>(response);
}

/**
 * Type-safe API client.
 *
 * All methods:
 * - Automatically include authorization header if token exists
 * - Extract data from ApiResponse wrapper
 * - Throw ApiError on failure
 * - Redirect to /login on 401
 *
 * @example
 * // GET request
 * const dashboard = await api.get<TodayDashboard>('/api/dashboard/today');
 *
 * // POST request
 * const result = await api.post<AuthResponse>('/api/auth/login', { email, password });
 *
 * // PUT request
 * await api.put('/api/weekly-plan', weeklyPlanData);
 *
 * // DELETE request
 * await api.delete('/api/tasks/123');
 */
export const api = {
  /**
   * Make a GET request.
   */
  get<T>(path: string, options?: RequestOptions): Promise<T> {
    return request<T>('GET', path, undefined, options);
  },

  /**
   * Make a POST request.
   */
  post<T>(path: string, body?: unknown, options?: RequestOptions): Promise<T> {
    return request<T>('POST', path, body, options);
  },

  /**
   * Make a PUT request.
   */
  put<T>(path: string, body?: unknown, options?: RequestOptions): Promise<T> {
    return request<T>('PUT', path, body, options);
  },

  /**
   * Make a DELETE request.
   */
  delete(path: string, options?: RequestOptions): Promise<void> {
    return request<void>('DELETE', path, undefined, options);
  },
};

export default api;
