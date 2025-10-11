import axiosInstance from '../api/axiosInstance';
import type { User } from '../types/user.interfaces';

export interface RegisterData {
    email: string;
    firstName: string;
    lastName: string;
    phoneNumber?: string;
    password: string;
    userType: 'student' | 'organizer';
    organizationName?: string;
}

export interface LoginData {
    email: string;
    password: string;
}

export interface AuthResponse {
    token: string;
    userId: number;
    email: string;
    firstName: string;
    lastName: string;
    phoneNumber?: string;
    userType: string;
}

class AuthService {
    async register(data: RegisterData): Promise<AuthResponse> {
        const response = await axiosInstance.post<AuthResponse>('/auth/register', data);
        return response.data;
    }

    async login(data: LoginData): Promise<AuthResponse> {
        const response = await axiosInstance.post<AuthResponse>('/auth/login', data);
        return response.data;
    }

    logout(): void {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    }

    getToken(): string | null {
        return localStorage.getItem('token');
    }

    isAuthenticated(): boolean {
        const token = this.getToken();
        return !!token;
    }
}

export default new AuthService();
