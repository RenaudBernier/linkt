import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import TwoFactorAuthPage from './TwoFactorAuthPage';
import { AuthProvider } from '../contexts/AuthContext';
import authService from '../services/authService';

// Mock the authService
vi.mock('../services/authService', () => ({
    default: {
        verify2FA: vi.fn(),
    },
}));

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

// Helper to render component with auth context
const renderWithAuth = (pendingAuthData: any) => {
    // Mock localStorage
    if (pendingAuthData) {
        localStorage.setItem('pendingAuth', JSON.stringify(pendingAuthData));
    }

    return render(
        <BrowserRouter>
            <AuthProvider>
                <TwoFactorAuthPage />
            </AuthProvider>
        </BrowserRouter>
    );
};

describe('TwoFactorAuthPage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    describe('Initial render and redirects', () => {
        it('should redirect to login if no pending auth', () => {
            renderWithAuth(null);
            expect(mockNavigate).toHaveBeenCalledWith('/login');
        });

        it('should redirect to login if pending auth status is not 2FA_REQUIRED', () => {
            renderWithAuth({ email: 'test@example.com', status: 'EMAIL_VERIFICATION_REQUIRED' });
            expect(mockNavigate).toHaveBeenCalledWith('/login');
        });

        it('should render the 2FA page if pending auth status is 2FA_REQUIRED', () => {
            renderWithAuth({ email: 'test@example.com', status: '2FA_REQUIRED' });

            expect(screen.getByText('Two-Factor Authentication')).toBeInTheDocument();
            expect(screen.getByText(/test@example.com/)).toBeInTheDocument();
        });
    });

    describe('Successful 2FA verification', () => {
        it('should redirect to home page on successful verification', async () => {
            const user = userEvent.setup();

            // Mock successful API response
            vi.mocked(authService.verify2FA).mockResolvedValueOnce({
                token: 'test-token',
                userId: 1,
                email: 'test@example.com',
                firstName: 'John',
                lastName: 'Doe',
                phoneNumber: '1234567890',
                userType: 'student',
            });

            renderWithAuth({ email: 'test@example.com', status: '2FA_REQUIRED' });

            // Enter 6-digit code (assumes CodeInput auto-submits on complete)
            const inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], String(i));
            }

            // Wait for verification to complete
            await waitFor(() => {
                expect(authService.verify2FA).toHaveBeenCalledWith({
                    email: 'test@example.com',
                    code: '012345',
                });
            });

            // Should navigate to home
            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith('/');
            });

            // Should store user data in localStorage
            expect(localStorage.getItem('token')).toBe('test-token');
            expect(localStorage.getItem('user')).toBeTruthy();
        });

        it('should NOT redirect to login on successful verification (regression test)', async () => {
            const user = userEvent.setup();

            vi.mocked(authService.verify2FA).mockResolvedValueOnce({
                token: 'test-token',
                userId: 1,
                email: 'test@example.com',
                firstName: 'John',
                lastName: 'Doe',
                userType: 'student',
            });

            renderWithAuth({ email: 'test@example.com', status: '2FA_REQUIRED' });

            // Clear any navigation calls from initial render
            mockNavigate.mockClear();

            const inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], '2');
            }

            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith('/');
            });

            // After successful verification, should ONLY have been called with '/' (not '/login')
            expect(mockNavigate).toHaveBeenCalledTimes(1);
            expect(mockNavigate).toHaveBeenCalledWith('/');
        });
    });

    describe('Failed 2FA verification', () => {
        it('should show error and stay on page when verification fails', async () => {
            const user = userEvent.setup();

            // Mock failed API response
            vi.mocked(authService.verify2FA).mockRejectedValueOnce({
                response: {
                    data: { message: 'Invalid verification code' },
                },
            });

            renderWithAuth({ email: 'test@example.com', status: '2FA_REQUIRED' });

            // Clear navigation calls from initial render
            mockNavigate.mockClear();

            const inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], '1');
            }

            // Wait for error to appear
            await waitFor(() => {
                expect(screen.getByText(/Invalid verification code/)).toBeInTheDocument();
            });

            // Should NOT navigate away after error
            expect(mockNavigate).not.toHaveBeenCalled();
        });

        it('should clear the code input on error to allow retry', async () => {
            const user = userEvent.setup();

            vi.mocked(authService.verify2FA).mockRejectedValueOnce({
                response: {
                    data: { message: 'Invalid verification code' },
                },
            });

            renderWithAuth({ email: 'test@example.com', status: '2FA_REQUIRED' });

            const inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], '1');
            }

            await waitFor(() => {
                expect(screen.getByText(/Invalid verification code/)).toBeInTheDocument();
            });

            // Code should be cleared for retry
            inputs.forEach(input => {
                expect(input).toHaveValue('');
            });
        });

        it('should handle expired code error', async () => {
            const user = userEvent.setup();

            vi.mocked(authService.verify2FA).mockRejectedValueOnce({
                response: {
                    data: { message: 'Code expired' },
                },
            });

            renderWithAuth({ email: 'test@example.com', status: '2FA_REQUIRED' });

            const inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], '1');
            }

            await waitFor(() => {
                expect(screen.getByText(/Code expired/)).toBeInTheDocument();
            });
        });

        it('should allow retry after failed verification', async () => {
            const user = userEvent.setup();

            // First attempt fails
            vi.mocked(authService.verify2FA)
                .mockRejectedValueOnce({
                    response: { data: { message: 'Invalid verification code' } },
                })
                // Second attempt succeeds
                .mockResolvedValueOnce({
                    token: 'test-token',
                    userId: 1,
                    email: 'test@example.com',
                    firstName: 'John',
                    lastName: 'Doe',
                    userType: 'student',
                });

            renderWithAuth({ email: 'test@example.com', status: '2FA_REQUIRED' });

            // First attempt
            let inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], '1');
            }

            await waitFor(() => {
                expect(screen.getByText(/Invalid verification code/)).toBeInTheDocument();
            });

            // Second attempt
            inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], '2');
            }

            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith('/');
            });
        });
    });

    describe('Back to Login button', () => {
        it('should navigate to login and clear pending auth when clicking back button', async () => {
            const user = userEvent.setup();

            renderWithAuth({ email: 'test@example.com', status: '2FA_REQUIRED' });

            const backButton = screen.getByText('Back to Login');
            await user.click(backButton);

            expect(mockNavigate).toHaveBeenCalledWith('/login');
            expect(localStorage.getItem('pendingAuth')).toBeNull();
        });
    });

    describe('Loading state', () => {
        it('should show loading overlay during verification', async () => {
            const user = userEvent.setup();

            // Mock API with delay
            vi.mocked(authService.verify2FA).mockImplementation(
                () => new Promise(resolve => setTimeout(() => resolve({
                    token: 'test-token',
                    userId: 1,
                    email: 'test@example.com',
                    userType: 'student',
                }), 100))
            );

            renderWithAuth({ email: 'test@example.com', status: '2FA_REQUIRED' });

            const inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], String(i));
            }

            // Should show loading message
            expect(screen.getByText(/Verifying your identity/)).toBeInTheDocument();
        });
    });
});
