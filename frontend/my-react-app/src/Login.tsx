import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './contexts/AuthContext';
import authService from './services/authService';

export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { login, setPendingAuth } = useAuth();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const response = await authService.login({ email, password });

            console.log('Login response:', response);
            console.log('User type from response:', response.userType);

            // Check if 2FA is required
            if (response.userType === '2FA_REQUIRED') {
                console.log('2FA required, redirecting to verification page');
                setPendingAuth(email, '2FA_REQUIRED');
                navigate('/verify-2fa');
                return;
            }

            // Direct login (admin or other immediate access)
            if (response.token) {
                const userData = {
                    firstName: response.firstName || '',
                    lastName: response.lastName || '',
                    email: response.email,
                    phoneNumber: response.phoneNumber || '',
                    userType: response.userType
                };

                console.log('User data being passed to login():', userData);

                // Update auth context with user data and token
                login(userData, response.token);

                console.log('After login, localStorage user:', localStorage.getItem('user'));

                navigate('/');
            } else {
                setError('Login failed. Please try again.');
            }
        } catch (err: any) {
            console.error('Login error:', err);
            const errorMessage = err.response?.data?.message || err.message;

            if (errorMessage.includes('email')) {
                setError('Please verify your email before logging in.');
            } else if (errorMessage.includes('credentials')) {
                setError('Invalid email or password.');
            } else {
                setError(errorMessage || 'Login failed. Please check your credentials.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: 400, margin: 'auto', padding: 20 }}>
            <h2>Log In</h2>
            {error && (
                <div
                    style={{
                        color: '#d32f2f',
                        backgroundColor: '#ffebee',
                        border: '1px solid #d32f2f',
                        padding: 12,
                        borderRadius: 4,
                        marginBottom: 15,
                        fontSize: 14
                    }}
                >
                    {error}
                </div>
            )}
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Email:</label>
                    <br />
                    <input
                        type="email"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        disabled={loading}
                        required
                        style={{
                            width: '100%',
                            padding: '8px',
                            marginTop: '4px',
                            boxSizing: 'border-box'
                        }}
                    />
                </div>
                <div style={{ marginTop: 15 }}>
                    <label>Password:</label>
                    <br />
                    <input
                        type="password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        disabled={loading}
                        required
                        style={{
                            width: '100%',
                            padding: '8px',
                            marginTop: '4px',
                            boxSizing: 'border-box'
                        }}
                    />
                </div>
                <button
                    type="submit"
                    disabled={loading}
                    style={{
                        marginTop: 20,
                        width: '100%',
                        padding: '10px',
                        backgroundColor: loading ? '#ccc' : '#288af3',
                        color: 'white',
                        border: 'none',
                        borderRadius: 4,
                        cursor: loading ? 'not-allowed' : 'pointer',
                        fontSize: 16
                    }}
                >
                    {loading ? 'Logging in...' : 'Log In'}
                </button>
            </form>
            <button
                style={{
                    marginTop: 15,
                    width: '100%',
                    padding: '10px',
                    backgroundColor: '#f5f5f5',
                    border: '1px solid #ddd',
                    borderRadius: 4,
                    cursor: 'pointer'
                }}
                onClick={() => navigate('/')}
                disabled={loading}
            >
                Back To Home
            </button>
        </div>
    );
}
