'use client';

import { useState, FormEvent } from 'react';
import { useLogin } from '@/hooks/useLogin';
import { ApiError } from '@/lib/api';
import { cn } from '@/lib/utils';

/**
 * Login form component.
 * Handles email/password input and form submission.
 */
export function LoginForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login, isLoading, error, reset } = useLogin();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    reset();

    try {
      await login({ email, password });
    } catch {
      // Error is handled by the hook
    }
  };

  const getErrorMessage = (err: ApiError | null): string | null => {
    if (!err) return null;

    if (err.status === 401) {
      return 'Invalid email or password';
    }
    if (err.status === 400) {
      return err.message || 'Please check your input';
    }
    return err.message || 'An error occurred';
  };

  const errorMessage = getErrorMessage(error);

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {errorMessage && (
        <div
          role="alert"
          className="p-3 rounded-md bg-destructive/10 text-destructive text-sm"
        >
          {errorMessage}
        </div>
      )}

      <div className="space-y-2">
        <label htmlFor="email" className="block text-sm font-medium">
          Email
        </label>
        <input
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          autoComplete="email"
          className={cn(
            'w-full px-3 py-2 rounded-md border border-border',
            'bg-background text-foreground',
            'focus:outline-none focus:ring-2 focus:ring-ring',
            'disabled:opacity-50 disabled:cursor-not-allowed'
          )}
          disabled={isLoading}
          placeholder="you@example.com"
        />
      </div>

      <div className="space-y-2">
        <label htmlFor="password" className="block text-sm font-medium">
          Password
        </label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          autoComplete="current-password"
          className={cn(
            'w-full px-3 py-2 rounded-md border border-border',
            'bg-background text-foreground',
            'focus:outline-none focus:ring-2 focus:ring-ring',
            'disabled:opacity-50 disabled:cursor-not-allowed'
          )}
          disabled={isLoading}
          placeholder="••••••••"
        />
      </div>

      <button
        type="submit"
        disabled={isLoading || !email || !password}
        className={cn(
          'w-full py-2 px-4 rounded-md font-medium',
          'bg-primary text-primary-foreground',
          'hover:bg-primary/90 transition-colors',
          'focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2',
          'disabled:opacity-50 disabled:cursor-not-allowed'
        )}
      >
        {isLoading ? 'Signing in...' : 'Sign in'}
      </button>
    </form>
  );
}

export default LoginForm;
