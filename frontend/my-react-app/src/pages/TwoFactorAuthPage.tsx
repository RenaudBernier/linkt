import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import authService from '../services/authService';
import CodeInput from '../components/CodeInput';
import { Backdrop, CircularProgress, Box } from '@mui/material';

export default function TwoFactorAuthPage() {
    const navigate = useNavigate();
    const { pendingAuth, clearPendingAuth, login } = useAuth();
    const [code, setCode] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const verificationSuccessful = useRef(false);

    useEffect(() => {
        // Redirect if no pending auth with 2FA_REQUIRED
        // BUT don't redirect if we just successfully verified (flag prevents race condition)
        if (!pendingAuth || pendingAuth.status !== '2FA_REQUIRED') {
            if (!verificationSuccessful.current) {
                navigate('/login');
            }
        }
    }, [pendingAuth, navigate]);

    if (!pendingAuth || pendingAuth.status !== '2FA_REQUIRED') {
        return null;
    }

    const handleCodeComplete = async (completedCode: string) => {
        setError('');
        setCode('');
        setLoading(true);

        try {
            const response = await authService.verify2FA({
                email: pendingAuth.email,
                code: completedCode
            });

            // Check if response has token (2FA successful)
            if (response.token) {
                // Store user data and token
                const userData = {
                    firstName: response.firstName || '',
                    lastName: response.lastName || '',
                    email: response.email,
                    phoneNumber: response.phoneNumber || '',
                    userType: response.userType
                };

                // Set flag to prevent useEffect from redirecting to login
                verificationSuccessful.current = true;

                // Login (which clears pendingAuth) then navigate to home
                login(userData, response.token);
                navigate('/');
            } else {
                setLoading(false);
                setError('Verification failed. Please try again.');
            }
        } catch (err: any) {
            setLoading(false);
            setCode('');
            const errorMessage = err.response?.data?.message || err.message;

            if (errorMessage.includes('expired')) {
                setError('Code expired. Please request a new one.');
            } else if (errorMessage.includes('Invalid') || errorMessage.includes('not found')) {
                setError('Invalid verification code.');
            } else {
                setError(errorMessage || 'An error occurred. Please try again.');
            }
        }
    };

    return (
        <>
            {/* Loading Overlay */}
            <Backdrop
                sx={{
                    color: '#fff',
                    backgroundColor: 'rgba(0, 0, 0, 0.7)',
                    zIndex: theme => theme.zIndex.drawer + 1,
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'center',
                    alignItems: 'center',
                    gap: 2
                }}
                open={loading}
            >
                <CircularProgress color="inherit" size={60} />
                <Box sx={{ fontSize: 18, fontWeight: 500, textAlign: 'center' }}>
                    Verifying your identity...
                </Box>
            </Backdrop>

            {/* Main Content */}
            <div style={{ maxWidth: 400, margin: 'auto', padding: 20 }}>
                <h2>Two-Factor Authentication</h2>
                <p style={{ color: '#666', marginBottom: 20 }}>
                    We've sent a verification code to <strong>{pendingAuth.email}</strong>
                </p>

                {error && (
                    <div
                        style={{
                            backgroundColor: '#ffebee',
                            border: '1px solid #d32f2f',
                            color: '#c62828',
                            padding: 12,
                            borderRadius: 4,
                            marginBottom: 20,
                            fontSize: 14
                        }}
                        data-testid="error-message"
                    >
                        {error}
                    </div>
                )}

                <form
                    onSubmit={e => {
                        e.preventDefault();
                    }}
                >
                    <CodeInput
                        value={code}
                        onChange={setCode}
                        onComplete={handleCodeComplete}
                        disabled={loading}
                        error={!!error}
                        placeholder="Enter 6-digit code"
                        length={6}
                    />

                    <p style={{ fontSize: 12, color: '#999', textAlign: 'center', marginTop: 20 }}>
                        Code will be automatically verified when all 6 digits are entered
                    </p>

                    <button
                        type="button"
                        onClick={() => {
                            clearPendingAuth();
                            navigate('/login');
                        }}
                        style={{
                            width: '100%',
                            padding: 10,
                            marginTop: 20,
                            backgroundColor: '#f5f5f5',
                            border: '1px solid #ddd',
                            borderRadius: 4,
                            cursor: 'pointer',
                            fontSize: 14
                        }}
                        disabled={loading}
                    >
                        Back to Login
                    </button>
                </form>
            </div>
        </>
    );
}
