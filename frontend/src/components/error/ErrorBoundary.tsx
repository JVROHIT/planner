'use client';

import React, { ErrorInfo } from 'react';
import { ApiError } from './ApiError';

/**
 * Global ErrorBoundary Component
 * 
 * Catches runtime errors in the component tree and displays a fallback UI.
 */

interface Props {
    children: React.ReactNode;
    fallback?: React.ReactNode;
}

interface State {
    hasError: boolean;
    error: Error | null;
}

export class ErrorBoundary extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = { hasError: false, error: null };
    }

    static getDerivedStateFromError(error: Error): State {
        return { hasError: true, error };
    }

    componentDidCatch(error: Error, errorInfo: ErrorInfo) {
        // Log error to an analytics service in a real app
        console.error('[ErrorBoundary] caught error:', error, errorInfo);
    }

    handleReset = () => {
        this.setState({ hasError: false, error: null });
    };

    render() {
        if (this.state.hasError) {
            if (this.props.fallback) {
                return this.props.fallback;
            }

            return (
                <div className="flex h-[400px] w-full items-center justify-center p-6">
                    <ApiError error={this.state.error} reset={this.handleReset} />
                </div>
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;
