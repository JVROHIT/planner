/**
 * Authentication token storage utilities.
 * Manages JWT token persistence in localStorage.
 */

const TOKEN_KEY = 'focusflow_token';
const USER_ID_KEY = 'focusflow_user_id';

/**
 * Check if running in browser environment.
 */
function isBrowser(): boolean {
  return typeof window !== 'undefined';
}

/**
 * Store authentication token and user ID.
 */
export function storeAuth(token: string, userId: string): void {
  if (!isBrowser()) return;
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_ID_KEY, userId);
}

/**
 * Get stored authentication token.
 */
export function getToken(): string | null {
  if (!isBrowser()) return null;
  return localStorage.getItem(TOKEN_KEY);
}

/**
 * Get stored user ID.
 */
export function getUserId(): string | null {
  if (!isBrowser()) return null;
  return localStorage.getItem(USER_ID_KEY);
}

/**
 * Clear all authentication data.
 */
export function clearAuth(): void {
  if (!isBrowser()) return;
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_ID_KEY);
}

/**
 * Check if user is authenticated (has token).
 */
export function isAuthenticated(): boolean {
  return getToken() !== null;
}
