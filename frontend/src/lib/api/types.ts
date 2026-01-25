/**
 * Standard API response wrapper from the backend.
 * All API responses follow this structure.
 */
export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  errorCode: string | null;
  message: string | null;
}

/**
 * API error class for typed error handling.
 */
export class ApiError extends Error {
  readonly status: number;
  readonly errorCode: string | null;

  constructor(message: string, status: number, errorCode: string | null = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.errorCode = errorCode;
  }

  /**
   * Check if error is an authentication error.
   */
  isUnauthorized(): boolean {
    return this.status === 401;
  }

  /**
   * Check if error is a forbidden error.
   */
  isForbidden(): boolean {
    return this.status === 403;
  }

  /**
   * Check if error is a not found error.
   */
  isNotFound(): boolean {
    return this.status === 404;
  }

  /**
   * Check if error is a conflict error.
   */
  isConflict(): boolean {
    return this.status === 409;
  }
}

/**
 * HTTP methods supported by the API client.
 */
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';

/**
 * Request options for API calls.
 */
export interface RequestOptions {
  headers?: Record<string, string>;
  signal?: AbortSignal;
}
