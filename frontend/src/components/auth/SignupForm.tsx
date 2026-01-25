'use client';

import { useState, FormEvent } from 'react';
import { useRegister } from '@/hooks/useRegister';
import { ApiError } from '@/lib/api';
import { cn } from '@/lib/utils';

/**
 * Signup form component.
 * Handles email/password input for registration.
 */
export function SignupForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [validationError, setValidationError] = useState<string | null>(null);
  const { register, isLoading, error, reset } = useRegister();

  const isPasswordDirty = password.length > 0 || confirmPassword.length > 0;
  const isPasswordValid = password.length >= 8 && password === confirmPassword;
  const showGreen = isPasswordDirty && isPasswordValid;
  const showRed = isPasswordDirty && !showGreen;

  const isFormValid = email.length > 0 && isPasswordValid;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!isFormValid) return;

    reset();
    setValidationError(null);

    try {
      await register({ email, password });
    } catch {
      // Error is handled by the hook
    }
  };

  const getErrorMessage = (err: ApiError | null): string | null => {
    if (!err) return null;

    if (err.status === 409) {
      return 'Email already exists';
    }
    if (err.status === 400) {
      return err.message || 'Please check your input';
    }
    return err.message || 'An error occurred';
  };

  const displayError = validationError || getErrorMessage(error);

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {displayError && (
        <div
          role="alert"
          className="p-3 rounded-md bg-destructive/10 text-destructive text-sm animate-in fade-in zoom-in-95"
        >
          {displayError}
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
            'disabled:opacity-50 disabled:cursor-not-allowed transition-all'
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
          minLength={8}
          autoComplete="new-password"
          className={cn(
            'w-full px-3 py-2 rounded-md border border-border',
            'bg-background text-foreground',
            'focus:outline-none focus:ring-2 focus:ring-ring',
            'disabled:opacity-50 disabled:cursor-not-allowed transition-all',
            showGreen && 'border-today-active ring-1 ring-today-active focus:ring-today-active',
            showRed && 'border-destructive ring-1 ring-destructive focus:ring-destructive'
          )}
          disabled={isLoading}
          placeholder="••••••••"
        />
        <p className="text-xs text-muted-foreground">
          At least 8 characters
        </p>
      </div>

      <div className="space-y-2">
        <label htmlFor="confirmPassword" className="block text-sm font-medium">
          Confirm Password
        </label>
        <input
          id="confirmPassword"
          type="password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          required
          autoComplete="new-password"
          className={cn(
            'w-full px-3 py-2 rounded-md border border-border',
            'bg-background text-foreground',
            'focus:outline-none focus:ring-2 focus:ring-ring',
            'disabled:opacity-50 disabled:cursor-not-allowed transition-all',
            showGreen && 'border-today-active ring-1 ring-today-active focus:ring-today-active',
            showRed && 'border-destructive ring-1 ring-destructive focus:ring-destructive'
          )}
          disabled={isLoading}
          placeholder="••••••••"
        />
      </div>

      <button
        type="submit"
        disabled={isLoading || !isFormValid}
        className={cn(
          'w-full py-2 px-4 rounded-md font-medium',
          'bg-primary text-primary-foreground',
          'hover:bg-primary/90 transition-colors',
          'focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2',
          'disabled:opacity-50 disabled:cursor-not-allowed'
        )}
      >
        {isLoading ? 'Creating account...' : 'Create account'}
      </button>
    </form>
  );
}

export default SignupForm;
