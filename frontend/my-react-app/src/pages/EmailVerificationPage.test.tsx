import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import EmailVerificationPage from './EmailVerificationPage';
import { AuthProvider } from '../contexts/AuthContext';
import authService from '../services/authService';

// Mock the authService
vi.mock('../services/authService', () => ({
    default: {
        verifyEmail: vi.fn(),
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
    if (pendingAuthData) {
        localStorage.setItem('pendingAuth', JSON.stringify(pendingAuthData));
    }

    return render(
        <BrowserRouter>
            <AuthProvider>
                <EmailVerificationPage />
            </AuthProvider>
        </BrowserRouter>
    );
};

describe('EmailVerificationPage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    describe('Initial render and redirects', () => {
        it('should redirect to signup if no pending auth', () => {
            renderWithAuth(null);
            expect(mockNavigate).toHaveBeenCalledWith('/signup');
        });

        it('should redirect to signup if pending auth status is not EMAIL_VERIFICATION_REQUIRED', () => {
            renderWithAuth({ email: 'test@example.com', status: '2FA_REQUIRED' });
            expect(mockNavigate).toHaveBeenCalledWith('/signup');
        });

        it('should render the email verification page if pending auth status is EMAIL_VERIFICATION_REQUIRED', () => {
            renderWithAuth({ email: 'test@example.com', status: 'EMAIL_VERIFICATION_REQUIRED' });

            expect(screen.getByText('Verify Your Email')).toBeInTheDocument();
            expect(screen.getByText(/test@example.com/)).toBeInTheDocument();
        });
    });

    describe('Successful email verification', () => {
        it('should redirect to home page on successful verification', async () => {
            const user = userEvent.setup();

            // Mock successful API response
            vi.mocked(authService.verifyEmail).mockResolvedValueOnce({
                token: 'test-token',
                userId: 1,
                email: 'test@example.com',
                firstName: 'Jane',
                lastName: 'Smith',
                phoneNumber: '9876543210',
                userType: 'organizer',
            });

            renderWithAuth({ email: 'test@example.com', status: 'EMAIL_VERIFICATION_REQUIRED' });

            // Enter 6-digit code
            const inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], '1');
            }

            // Wait for verification to complete
            await waitFor(() => {
                expect(authService.verifyEmail).toHaveBeenCalledWith({
                    email: 'test@example.com',
                    code: '111111',
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

        it('should NOT redirect to signup on successful verification (regression test)', async () => {
            const user = userEvent.setup();

            vi.mocked(authService.verifyEmail).mockResolvedValueOnce({
                token: 'test-token',
                userId: 1,
                email: 'test@example.com',
                firstName: 'Jane',
                lastName: 'Smith',
                userType: 'organizer',
            });

            renderWithAuth({ email: 'test@example.com', status: 'EMAIL_VERIFICATION_REQUIRED' });

            // Clear any navigation calls from initial render
            mockNavigate.mockClear();

            const inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], '2');
            }

            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith('/');
            });

            // After successful verification, should ONLY have been called with '/' (not '/signup')
            expect(mockNavigate).toHaveBeenCalledTimes(1);
            expect(mockNavigate).toHaveBeenCalledWith('/');
        });
    });

    describe('Failed email verification', () => {
        it('should show error and stay on page when verification fails', async () => {
            const user = userEvent.setup();

            // Mock failed API response
            vi.mocked(authService.verifyEmail).mockRejectedValueOnce({
                response: {
                    data: { message: 'Invalid verification code' },
                },
            });

            renderWithAuth({ email: 'test@example.com', status: 'EMAIL_VERIFICATION_REQUIRED' });

            // Clear navigation calls from initial render
            mockNavigate.mockClear();

            const inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], '9');
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

            vi.mocked(authService.verifyEmail).mockRejectedValueOnce({
                response: {
                    data: { message: 'Invalid verification code' },
                },
            });

            renderWithAuth({ email: 'test@example.com', status: 'EMAIL_VERIFICATION_REQUIRED' });

            const inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], '7');
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

            vi.mocked(authService.verifyEmail).mockRejectedValueOnce({
                response: {
                    data: { message: 'Code expired' },
                },
            });

            renderWithAuth({ email: 'test@example.com', status: 'EMAIL_VERIFICATION_REQUIRED' });

            const inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], '3');
            }

            await waitFor(() => {
                expect(screen.getByText(/Code expired/)).toBeInTheDocument();
            });
        });

        it('should allow retry after failed verification', async () => {
            const user = userEvent.setup();

            // First attempt fails
            vi.mocked(authService.verifyEmail)
                .mockRejectedValueOnce({
                    response: { data: { message: 'Invalid verification code' } },
                })
                // Second attempt succeeds
                .mockResolvedValueOnce({
                    token: 'test-token',
                    userId: 1,
                    email: 'test@example.com',
                    firstName: 'Jane',
                    lastName: 'Smith',
                    userType: 'organizer',
                });

            renderWithAuth({ email: 'test@example.com', status: 'EMAIL_VERIFICATION_REQUIRED' });

            // First attempt
            let inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], '4');
            }

            await waitFor(() => {
                expect(screen.getByText(/Invalid verification code/)).toBeInTheDocument();
            });

            // Second attempt
            inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], '5');
            }

            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith('/');
            });
        });
    });

    describe('Back to Login button', () => {
        it('should navigate to login when clicking back button', async () => {
            const user = userEvent.setup();

            renderWithAuth({ email: 'test@example.com', status: 'EMAIL_VERIFICATION_REQUIRED' });

            const backButton = screen.getByText('Back to Login');
            await user.click(backButton);

            expect(mockNavigate).toHaveBeenCalledWith('/login');
        });
    });

    describe('Loading state', () => {
        it('should show loading overlay during verification', async () => {
            const user = userEvent.setup();

            // Mock API with delay
            vi.mocked(authService.verifyEmail).mockImplementation(
                () => new Promise(resolve => setTimeout(() => resolve({
                    token: 'test-token',
                    userId: 1,
                    email: 'test@example.com',
                    userType: 'student',
                }), 100))
            );

            renderWithAuth({ email: 'test@example.com', status: 'EMAIL_VERIFICATION_REQUIRED' });

            const inputs = screen.getAllByRole('textbox');
            for (let i = 0; i < 6; i++) {
                await user.type(inputs[i], String(i));
            }

            // Should show loading message
            expect(screen.getByText(/Verifying your email/)).toBeInTheDocument();
        });
    });
});
