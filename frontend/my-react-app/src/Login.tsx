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
        <div style = {{backgroundColor: '#373f51', display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh'}}>
        <div className = 'loginDiv' style={{ maxWidth: 500, fontSize: '25px', margin: 'auto', padding: 20 , border: '1px solid #ccc', borderRadius: 8, boxShadow: '0 2px 4px rgba(0,0,0,0.1)', backgroundColor: '#a63a50'}}>
            <h2 style = {{fontSize: '60px', fontFamily: 'Montserrat', fontStyle: 'italic', color: 'white'}}>Log In</h2>
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
                <div style = {{fontSize: '25px', color: 'white', height: '100px'}}>
                    <label>Email:</label><br/>
                    <input
                        type="email"
                        size={40}
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
                <div style={{ marginTop: 15, color: 'white' }}>
                    <label>Password:</label>
                    <br />
                    <input
                        type="password"
                        size={40}
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
                        fontSize: 20
                    }}
                >
                    {loading ? 'Logging in...' : 'Log In'}
                </button>
            </form>
            <button
                style={{
                    marginTop: 20,
                    width: '100%',
                    padding: '10px',
                    backgroundColor: '#288af3',
                    color: 'white',
                    fontSize: 20,
                    borderRadius: 4,
                    cursor: 'pointer'
                }}
                onClick={() => navigate('/')}
                disabled={loading}
            >
                Back To Home
            </button>
        </div>
        </div>
    );
}
