'use client';

import { getErrorMessage } from '@/lib/errors';
import { ApiError as ApiLibraryError } from '@/lib/api';

/**
 * ApiError Component
 * 
 * Renders a consistent error message for API failures.
 * 
 * @param error The error object (expected to be ApiError)
 * @param reset Optional callback to retry the operation
 */

interface ApiErrorProps {
    error: Error | ApiLibraryError | unknown;
    reset?: () => void;
}

export function ApiError({ error, reset }: ApiErrorProps) {
    let title = 'Unexpected Error';
    let message = 'An unknown error occurred.';

    if (error instanceof ApiLibraryError) {
        const errorInfo = getErrorMessage(error.status, error.errorCode, error.message);
        title = errorInfo.title;
        message = errorInfo.message;
    } else if (error instanceof Error) {
        message = error.message;
    }

    return (
        <div className="flex flex-col items-center justify-center p-8 text-center bg-destructive/10 border border-destructive/20 rounded-xl space-y-4">
            <div className="w-12 h-12 flex items-center justify-center rounded-full bg-destructive/20 text-destructive">
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    strokeWidth={1.5}
                    stroke="currentColor"
                    className="w-6 h-6"
                >
                    <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.731 0 2.814-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z"
                    />
                </svg>
            </div>

            <div className="space-y-1">
                <h3 className="text-lg font-semibold text-destructive">{title}</h3>
                <p className="text-sm text-muted-foreground max-w-xs mx-auto">
                    {message}
                </p>
            </div>

            {reset && (
                <button
                    onClick={reset}
                    className="px-4 py-2 bg-destructive text-destructive-foreground rounded-lg hover:bg-destructive/90 transition-colors text-sm font-medium"
                >
                    Try Again
                </button>
            )}
        </div>
    );
}

export default ApiError;
