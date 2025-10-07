import type { User } from '../types/user.interfaces';

export interface SignUpData extends User {
    password: string;
    userType: 'student' | 'org';
    organizationName?: string;
}

/**
 * Sign up a new user
 * @param data - User signup data
 * @returns Promise<boolean> - true if successful, false if error
 */
export const signUp = async (data: SignUpData): Promise<boolean> => {
    try {
        // Dummy implementation - simulate API call
        await new Promise(resolve => setTimeout(resolve, 500));

        // Simulate successful signup
        console.log('Signup request:', data);
        return true;
    } catch (error) {
        console.error('Signup error:', error);
        return false;
    }
};
