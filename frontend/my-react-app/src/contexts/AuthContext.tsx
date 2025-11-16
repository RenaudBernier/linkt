import React, { createContext, useContext, useState, useEffect } from 'react';
import type { User } from '../types/user.interfaces';

export interface PendingAuth {
    email: string;
    status: 'EMAIL_VERIFICATION_REQUIRED' | '2FA_REQUIRED';
}

interface AuthContextType {
    user: User | null;
    token: string | null;
    isAuthenticated: boolean;
    pendingAuth: PendingAuth | null;
    login: (userData: User, token: string) => void;
    logout: () => void;
    setPendingAuth: (email: string, status: 'EMAIL_VERIFICATION_REQUIRED' | '2FA_REQUIRED') => void;
    clearPendingAuth: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [token, setToken] = useState<string | null>(null);
    const [pendingAuth, setPendingAuthState] = useState<PendingAuth | null>(null);

    // Load user and token from localStorage on mount
    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        const storedToken = localStorage.getItem('token');
        const storedPendingAuth = localStorage.getItem('pendingAuth');

        if (storedUser && storedToken) {
            setUser(JSON.parse(storedUser));
            setToken(storedToken);
        }

        if (storedPendingAuth) {
            setPendingAuthState(JSON.parse(storedPendingAuth));
        }
    }, []);

    const login = (userData: User, authToken: string) => {
        setUser(userData);
        setToken(authToken);
        setPendingAuthState(null);
        localStorage.setItem('user', JSON.stringify(userData));
        localStorage.setItem('token', authToken);
        localStorage.removeItem('pendingAuth');
    };

    const logout = () => {
        setUser(null);
        setToken(null);
        setPendingAuthState(null);
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        localStorage.removeItem('pendingAuth');
    };

    const setPendingAuth = (email: string, status: 'EMAIL_VERIFICATION_REQUIRED' | '2FA_REQUIRED') => {
        const pendingData: PendingAuth = { email, status };
        setPendingAuthState(pendingData);
        localStorage.setItem('pendingAuth', JSON.stringify(pendingData));
    };

    const clearPendingAuth = () => {
        setPendingAuthState(null);
        localStorage.removeItem('pendingAuth');
    };

    return (
        <AuthContext.Provider
            value={{
                user,
                token,
                isAuthenticated: !!user && !!token,
                pendingAuth,
                login,
                logout,
                setPendingAuth,
                clearPendingAuth
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
