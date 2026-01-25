import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';
import { SignupForm } from '../SignupForm';

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

describe('SignupForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders email, password, and confirm password inputs', () => {
    const Wrapper = createWrapper();
    render(<SignupForm />, { wrapper: Wrapper });

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^password$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/confirm password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
  });

  it('disables submit button when fields are empty', () => {
    const Wrapper = createWrapper();
    render(<SignupForm />, { wrapper: Wrapper });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    expect(submitButton).toBeDisabled();
  });

  it('enables submit button when all fields are filled', async () => {
    const user = userEvent.setup();
    const Wrapper = createWrapper();
    render(<SignupForm />, { wrapper: Wrapper });

    await user.type(screen.getByLabelText(/email/i), 'new@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'password123');
    await user.type(screen.getByLabelText(/confirm password/i), 'password123');

    const submitButton = screen.getByRole('button', { name: /create account/i });
    expect(submitButton).not.toBeDisabled();
  });

  it('shows validation error when passwords do not match', async () => {
    const user = userEvent.setup();
    const Wrapper = createWrapper();
    render(<SignupForm />, { wrapper: Wrapper });

    await user.type(screen.getByLabelText(/email/i), 'new@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'password123');
    await user.type(screen.getByLabelText(/confirm password/i), 'different123');

    const submitButton = screen.getByRole('button', { name: /create account/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(/passwords do not match/i);
    });
  });

  it('shows validation error when password is too short', async () => {
    const user = userEvent.setup();
    const Wrapper = createWrapper();
    render(<SignupForm />, { wrapper: Wrapper });

    await user.type(screen.getByLabelText(/email/i), 'new@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'short');
    await user.type(screen.getByLabelText(/confirm password/i), 'short');

    const submitButton = screen.getByRole('button', { name: /create account/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(/at least 8 characters/i);
    });
  });

  it('calls register on valid form submission', async () => {
    const { api } = await import('@/lib/api');
    vi.mocked(api.post).mockResolvedValueOnce({ token: 'jwt-token', userId: 'user-123' });

    const user = userEvent.setup();
    const Wrapper = createWrapper();
    render(<SignupForm />, { wrapper: Wrapper });

    await user.type(screen.getByLabelText(/email/i), 'new@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'password123');
    await user.type(screen.getByLabelText(/confirm password/i), 'password123');

    const submitButton = screen.getByRole('button', { name: /create account/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/api/auth/register', {
        email: 'new@example.com',
        password: 'password123',
      });
    });
  });

  it('displays error on 409 duplicate email', async () => {
    const { api, ApiError } = await import('@/lib/api');
    vi.mocked(api.post).mockRejectedValueOnce(
      new ApiError('Email already exists', 409, 'CONFLICT')
    );

    const user = userEvent.setup();
    const Wrapper = createWrapper();
    render(<SignupForm />, { wrapper: Wrapper });

    await user.type(screen.getByLabelText(/email/i), 'existing@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'password123');
    await user.type(screen.getByLabelText(/confirm password/i), 'password123');

    const submitButton = screen.getByRole('button', { name: /create account/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(/email already exists/i);
    });
  });

  it('shows loading state during submission', async () => {
    const { api } = await import('@/lib/api');
    let resolvePromise: (value: unknown) => void;
    const pendingPromise = new Promise((resolve) => {
      resolvePromise = resolve;
    });
    vi.mocked(api.post).mockReturnValueOnce(pendingPromise as Promise<unknown>);

    const user = userEvent.setup();
    const Wrapper = createWrapper();
    render(<SignupForm />, { wrapper: Wrapper });

    await user.type(screen.getByLabelText(/email/i), 'new@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'password123');
    await user.type(screen.getByLabelText(/confirm password/i), 'password123');

    const submitButton = screen.getByRole('button', { name: /create account/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /creating account/i })).toBeInTheDocument();
    });

    // Cleanup
    resolvePromise!({ token: 'jwt-token', userId: 'user-123' });
  });
});
