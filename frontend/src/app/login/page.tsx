import Link from 'next/link';

/**
 * Login page - Access Mode.
 * Authentication entry point.
 */
export default function LoginPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-muted/30">
      <div className="w-full max-w-md p-8 bg-background rounded-lg border border-border shadow-sm">
        <div className="text-center mb-8">
          <h1 className="text-2xl font-bold">FocusFlow</h1>
          <p className="text-muted-foreground mt-2">Sign in to your account</p>
        </div>

        <p className="text-muted-foreground text-center">
          Login form will be implemented in Phase 2.
        </p>

        <div className="mt-6 text-center text-sm">
          <span className="text-muted-foreground">Don&apos;t have an account? </span>
          <Link href="/signup" className="text-primary hover:underline">
            Sign up
          </Link>
        </div>
      </div>
    </div>
  );
}
