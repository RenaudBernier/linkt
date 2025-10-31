import axios from 'axios';
import axiosInstance from '../api/axiosInstance';

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
    userType: 'student' | 'organizer' | 'admin';
}

class AuthService {
    async register(data: RegisterData): Promise<AuthResponse> {
        try {
            const response = await axiosInstance.post<AuthResponse>('/auth/register', data);
            return response.data;
        } catch (error) {
            if (axios.isAxiosError(error)) {
                console.error('Registration error:', error.response?.data || error.message);
            } else {
                console.error('Registration error:', error);
            }
            throw error;
        }
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
