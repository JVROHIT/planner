'use client';

import { useMemo, useState, FormEvent } from 'react';
import { useRegister } from '@/hooks/useRegister';
import { ApiError } from '@/lib/api';
import { cn } from '@/lib/utils';

type GoalInput = {
  title: string;
  horizon: 'MONTH' | 'QUARTER' | 'YEAR';
};

const getLocalISODate = () => new Date().toLocaleDateString('en-CA');

const getWeekStart = (dateValue: string) => {
  const base = new Date(`${dateValue}T00:00:00`);
  const day = base.getDay();
  const diff = (day + 6) % 7; // Monday as week start
  base.setDate(base.getDate() - diff);
  return base.toLocaleDateString('en-CA');
};

/**
 * Signup form component.
 * Handles email/password input for registration.
 */
export function SignupForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [weekStart, setWeekStart] = useState(() => getWeekStart(getLocalISODate()));
  const [goals, setGoals] = useState<GoalInput[]>([{ title: '', horizon: 'MONTH' }]);
  const [validationError, setValidationError] = useState<string | null>(null);
  const { register, isLoading, error, reset } = useRegister();

  const isPasswordDirty = password.length > 0 || confirmPassword.length > 0;
  const isPasswordValid = password.length >= 8 && password === confirmPassword;
  const showGreen = isPasswordDirty && isPasswordValid;
  const showRed = isPasswordDirty && !showGreen;

  const isFormValid = email.length > 0 && isPasswordValid && weekStart.length > 0;

  const goalCount = useMemo(
    () => goals.filter((goal) => goal.title.trim().length > 0).length,
    [goals]
  );

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!email.trim()) {
      setValidationError('Email is required');
      return;
    }
    if (password.length < 8) {
      setValidationError('Password must be at least 8 characters');
      return;
    }
    if (password !== confirmPassword) {
      setValidationError('Passwords do not match');
      return;
    }
    if (!weekStart) {
      setValidationError('Select a week start date');
      return;
    }

    reset();
    setValidationError(null);

    try {
      const goalsPayload = goals
        .filter((goal) => goal.title.trim().length > 0)
        .map((goal) => ({
          title: goal.title.trim(),
          horizon: goal.horizon,
        }));

      const normalizedWeekStart = getWeekStart(weekStart);

      const payload: {
        email: string;
        password: string;
        weekStart: string;
        goals?: { title: string; horizon: GoalInput['horizon'] }[];
      } = {
        email: email.trim(),
        password,
        weekStart: normalizedWeekStart,
      };

      if (goalsPayload.length > 0) {
        payload.goals = goalsPayload;
      }

      await register(payload);
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

      <div className="space-y-2">
        <label htmlFor="weekStart" className="block text-sm font-medium">
          Week plan start date
        </label>
        <input
          id="weekStart"
          type="date"
          value={weekStart}
          onChange={(e) => setWeekStart(e.target.value)}
          required
          className={cn(
            'w-full px-3 py-2 rounded-md border border-border',
            'bg-background text-foreground',
            'focus:outline-none focus:ring-2 focus:ring-ring',
            'disabled:opacity-50 disabled:cursor-not-allowed transition-all'
          )}
          disabled={isLoading}
        />
        <p className="text-xs text-muted-foreground">
          Pick any date and we will start from the week of that date (Monday start).
        </p>
      </div>

      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <label className="block text-sm font-medium">Goals (optional)</label>
            {goalCount > 0 && (
              <span className="text-xs text-muted-foreground">
                {goalCount} goal{goalCount === 1 ? '' : 's'} ready
              </span>
            )}
          </div>
          <button
            type="button"
            onClick={() => setGoals((prev) => [...prev, { title: '', horizon: 'MONTH' }])}
            className="text-xs font-medium text-primary hover:underline"
            disabled={isLoading}
          >
            Add goal
          </button>
        </div>

        {goals.map((goal, index) => (
          <div
            key={`goal-${index}`}
            className="space-y-2 rounded-md border border-border bg-muted/30 p-3"
          >
            <div className="flex items-start gap-2">
              <input
                type="text"
                value={goal.title}
                onChange={(e) => {
                  const next = [...goals];
                  next[index] = { ...next[index], title: e.target.value };
                  setGoals(next);
                }}
                placeholder={`Goal ${index + 1} title`}
                className={cn(
                  'flex-1 px-3 py-2 rounded-md border border-border',
                  'bg-background text-foreground',
                  'focus:outline-none focus:ring-2 focus:ring-ring',
                  'disabled:opacity-50 disabled:cursor-not-allowed transition-all'
                )}
                disabled={isLoading}
              />
              {goals.length > 1 && (
                <button
                  type="button"
                  onClick={() => setGoals((prev) => prev.filter((_, i) => i !== index))}
                  className="text-xs text-destructive hover:underline mt-2"
                  disabled={isLoading}
                >
                  Remove
                </button>
              )}
            </div>

            <div className="flex items-center gap-2">
              <label className="text-xs text-muted-foreground">Horizon</label>
              <select
                value={goal.horizon}
                onChange={(e) => {
                  const next = [...goals];
                  next[index] = { ...next[index], horizon: e.target.value as GoalInput['horizon'] };
                  setGoals(next);
                }}
                className={cn(
                  'px-2 py-1 rounded-md border border-border bg-background text-foreground',
                  'text-xs focus:outline-none focus:ring-2 focus:ring-ring'
                )}
                disabled={isLoading}
              >
                <option value="MONTH">Month</option>
                <option value="QUARTER">Quarter</option>
                <option value="YEAR">Year</option>
              </select>
            </div>
          </div>
        ))}
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
