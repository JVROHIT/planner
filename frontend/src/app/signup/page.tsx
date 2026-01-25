import Link from 'next/link';
import { SignupForm } from '@/components/auth';

/**
 * Signup page - Access Mode.
 * New user registration.
 */
export default function SignupPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-muted/30">
      <div className="w-full max-w-md p-8 bg-background rounded-lg border border-border shadow-sm">
        <div className="text-center mb-8">
          <div className="w-12 h-12 mx-auto mb-4 rounded-xl bg-primary flex items-center justify-center">
            <span className="text-primary-foreground font-bold text-lg">FF</span>
          </div>
          <h1 className="text-2xl font-bold">Get started</h1>
          <p className="text-muted-foreground mt-2">Create your account</p>
        </div>

        <SignupForm />

        <div className="mt-6 text-center text-sm">
          <span className="text-muted-foreground">Already have an account? </span>
          <Link href="/login" className="text-primary hover:underline">
            Sign in
          </Link>
        </div>
      </div>
    </div>
  );
}
