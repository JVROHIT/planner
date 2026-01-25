import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { LoginForm } from '../LoginForm';

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

describe('LoginForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders email and password inputs', () => {
    const Wrapper = createWrapper();
    render(<LoginForm />, { wrapper: Wrapper });

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('disables submit button when fields are empty', () => {
    const Wrapper = createWrapper();
    render(<LoginForm />, { wrapper: Wrapper });

    const submitButton = screen.getByRole('button', { name: /sign in/i });
    expect(submitButton).toBeDisabled();
  });

  it('enables submit button when fields are filled', async () => {
    const user = userEvent.setup();
    const Wrapper = createWrapper();
    render(<LoginForm />, { wrapper: Wrapper });

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');

    const submitButton = screen.getByRole('button', { name: /sign in/i });
    expect(submitButton).not.toBeDisabled();
  });

  it('calls login on form submission', async () => {
    const { api } = await import('@/lib/api');
    vi.mocked(api.post).mockResolvedValueOnce({ token: 'jwt-token', userId: 'user-123' });

    const user = userEvent.setup();
    const Wrapper = createWrapper();
    render(<LoginForm />, { wrapper: Wrapper });

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/api/auth/login', {
        email: 'test@example.com',
        password: 'password123',
      });
    });
  });

  it('displays error message on 401 response', async () => {
    const { api, ApiError } = await import('@/lib/api');
    vi.mocked(api.post).mockRejectedValueOnce(
      new ApiError('Unauthorized', 401, 'UNAUTHORIZED')
    );

    const user = userEvent.setup();
    const Wrapper = createWrapper();
    render(<LoginForm />, { wrapper: Wrapper });

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'wrongpassword');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(/invalid email or password/i);
    });
  });

  it('shows loading state during submission', async () => {
    const { api } = await import('@/lib/api');
    // Create a promise that won't resolve immediately
    let resolvePromise: (value: unknown) => void;
    const pendingPromise = new Promise((resolve) => {
      resolvePromise = resolve;
    });
    vi.mocked(api.post).mockReturnValueOnce(pendingPromise as Promise<unknown>);

    const user = userEvent.setup();
    const Wrapper = createWrapper();
    render(<LoginForm />, { wrapper: Wrapper });

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /signing in/i })).toBeInTheDocument();
    });

    // Cleanup
    resolvePromise!({ token: 'jwt-token', userId: 'user-123' });
  });
});
