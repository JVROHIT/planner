'use client';

import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import { cn } from '@/lib/utils';

/**
 * Toast Provider
 * 
 * A simple context-based toast system for notifications.
 * Philosophy: Feedback should be immediate and clear.
 */

type ToastType = 'success' | 'error' | 'info';

interface Toast {
    id: string;
    type: ToastType;
    title?: string;
    message: string;
}

interface ToastContextType {
    showToast: (toast: Omit<Toast, 'id'>) => void;
    success: (message: string, title?: string) => void;
    error: (message: string, title?: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export function ToastProvider({ children }: { children: ReactNode }) {
    const [toasts, setToasts] = useState<Toast[]>([]);

    const showToast = useCallback(({ type, title, message }: Omit<Toast, 'id'>) => {
        const id = Math.random().toString(36).substring(2, 9);
        setToasts((prev) => [...prev, { id, type, title, message }]);

        // Auto-remove after 5 seconds
        setTimeout(() => {
            setToasts((prev) => prev.filter((t) => t.id !== id));
        }, 5000);
    }, []);

    const success = useCallback((message: string, title?: string) => {
        showToast({ type: 'success', title, message });
    }, [showToast]);

    const error = useCallback((message: string, title?: string) => {
        showToast({ type: 'error', title, message });
    }, [showToast]);

    return (
        <ToastContext.Provider value={{ showToast, success, error }}>
            {children}

            {/* Toast container */}
            <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 pointer-events-none">
                {toasts.map((toast) => (
                    <div
                        key={toast.id}
                        className={cn(
                            'pointer-events-auto min-w-[300px] max-w-sm p-4 rounded-lg shadow-lg border transition-all animate-in slide-in-from-right-full',
                            toast.type === 'success' && 'bg-success/10 border-success/20 text-success-foreground',
                            toast.type === 'error' && 'bg-destructive/10 border-destructive/20 text-destructive-foreground',
                            toast.type === 'info' && 'bg-blue-100 border-blue-200 text-blue-800'
                        )}
                    >
                        {toast.title && <h4 className="font-bold text-sm mb-1">{toast.title}</h4>}
                        <p className="text-sm">{toast.message}</p>
                    </div>
                ))}
            </div>
        </ToastContext.Provider>
    );
}

export function useToast() {
    const context = useContext(ToastContext);
    if (!context) {
        throw new Error('useToast must be used within a ToastProvider');
    }
    return context;
}
