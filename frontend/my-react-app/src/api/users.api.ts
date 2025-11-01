import type { User } from '../types/user.interfaces';

import authService, { type AuthResponse } from '../services/authService';
import axiosInstance from './axiosInstance';

export interface SignUpData extends User {
    password: string;
    userType: 'student' | 'org';
    organizationName?: string;
}

/**
 * Sign up a new user
 * @param data - User signup data
 * @returns Promise<AuthResponse> - Auth response with token and user data
 */
export const signUp = async (data: SignUpData): Promise<AuthResponse> => {
    const registerData = {
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        phoneNumber: data.phoneNumber,
        password: data.password,
        userType: (data.userType === 'org' ? 'organizer' : 'student') as 'student' | 'organizer',
        organizationName: data.organizationName
    };

    const response = await authService.register(registerData);

    // Store token and user data in localStorage
    localStorage.setItem('token', response.token);
    localStorage.setItem('user', JSON.stringify({
        userId: response.userId,
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        phoneNumber: response.phoneNumber,
        userType: response.userType
    }));

    return response;
};

export interface CurrentUser {
    userId: number;
    email: string;
    firstName: string;
    lastName: string;
    phoneNumber?: string;
    userType: 'student' | 'organizer' | 'administrator';
}

export const getCurrentUser = async (): Promise<CurrentUser> => {
    const response = await axiosInstance.get('/users/me');
    return response.data;
};

export const getPendingOrganizers = async () => {
    return await axiosInstance.get('/users/pending-organizers');
}

export const approveOrganizer = async (userId: number) => {
    return await axiosInstance.put(`/users/approve-organizer/${userId}`);
}
