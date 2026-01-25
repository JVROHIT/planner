'use client';

import { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import { getToken, getUserId, clearAuth, storeAuth as persistAuth } from '@/lib/auth/storage';

interface User {
    id: string;
}

interface AuthContextType {
    user: User | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (token: string, userId: string) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
    const router = useRouter();

    const [user, setUser] = useState<User | null>(null);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    // Sync state with storage on mount
    useEffect(() => {
        const token = getToken();
        const userId = getUserId();

        if (token && userId) {
            setUser({ id: userId });
            setIsAuthenticated(true);
        }
        setIsLoading(false);
    }, []);

    const login = useCallback((token: string, userId: string) => {
        persistAuth(token, userId);
        setUser({ id: userId });
        setIsAuthenticated(true);
        router.push('/today');
    }, [router]);

    const logout = useCallback(() => {
        clearAuth();
        setUser(null);
        setIsAuthenticated(false);
        router.push('/login');
    }, [router]);

    return (
        <AuthContext.Provider value={{ user, isAuthenticated, isLoading, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}
