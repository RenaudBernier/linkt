import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { signUp } from './api/users.api';
import { useAuth } from './contexts/AuthContext';
import { Toolbar, Box, Container, Typography } from "@mui/material";
import './SignUp.css';

export default function SignUp() {
    const [name, setName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [password, setPassword] = useState('');
    const [userType, setUserType] = useState<'student' | 'org'>('student');
    const [organizationName, setOrganizationName] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { login, setPendingAuth } = useAuth();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (password.length < 7) {
            setError('Password must be at least 7 characters long.');
            return;
        }

        setLoading(true);

        try {
            const response = await signUp({
                firstName: name,
                lastName: lastName,
                email: email,
                password: password,
                phoneNumber: phoneNumber,
                userType: userType,
                organizationName: userType === 'org' ? organizationName : undefined
            });

            console.log('Signup response:', response);

            // Check if email verification is required
            if (response.userType === 'EMAIL_VERIFICATION_REQUIRED') {
                console.log('Email verification required, redirecting to verification page');
                setPendingAuth(email, 'EMAIL_VERIFICATION_REQUIRED');
                navigate('/verify-email');
            } else if (response.token) {
                // Direct registration (shouldn't happen based on backend)
                login({
                    firstName: response.firstName || '',
                    lastName: response.lastName || '',
                    email: response.email,
                    phoneNumber: response.phoneNumber || '',
                    userType: response.userType
                }, response.token);

                navigate('/');
            }
        } catch (error: any) {
            console.error('Signup failed!', error);
            const errorMessage = error.response?.data?.message || error.message;

            if (errorMessage.includes('already exists')) {
                setError('Email already registered. Please log in instead.');
            } else if (errorMessage.includes('required')) {
                setError(errorMessage);
            } else {
                setError(errorMessage || 'Registration failed. Please try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className='signup-container'>
            <h2 className='signuptitle'>SIGN UP</h2>
            {error && (
                <div
                    style={{
                        backgroundColor: '#ffebee',
                        border: '1px solid #d32f2f',
                        color: '#c62828',
                        padding: 12,
                        borderRadius: 4,
                        marginBottom: 15,
                        fontSize: 14
                    }}
                >
                    {error}
                </div>
            )}
            <form onSubmit={handleSubmit} className='signup-form'>
                <div>
                    <label>Name:</label>
                    <br />
                    <input
                        type="text"
                        value={name}
                        onChange={e => setName(e.target.value)}
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
                <div style={{ marginTop: 10 }}>
                    <label>Last Name:</label>
                    <br />
                    <input
                        type="text"
                        value={lastName}
                        onChange={e => setLastName(e.target.value)}
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
                <div style={{ marginTop: 10 }}>
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
                <div style={{ marginTop: 10 }}>
                    <label>Phone Number:</label>
                    <br />
                    <input
                        type="tel"
                        value={phoneNumber}
                        onChange={e => setPhoneNumber(e.target.value)}
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
                <div style={{ marginTop: 10, marginBottom: 20 }}>
                    <label>
                        Password: <span style={{ fontSize: 12}}>(Min 7 characters)</span>
                    </label>
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
                {userType === 'org' && (
                    <div style={{ marginBottom: 20 }}>
                        <label>Organization Name:</label>
                        <br />
                        <input
                            type="text"
                            value={organizationName}
                            onChange={e => setOrganizationName(e.target.value)}
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
                )}
                <div
                    style={{
                        position: 'relative',
                        width: '100%',
                        maxWidth: 300,
                        margin: '0 auto 20px',
                        height: 40,
                        borderRadius: 20,
                        backgroundColor: '#e0e0e0',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        padding: 4
                    }}
                >
                    <div
                        style={{
                            position: 'absolute',
                            top: 4,
                            left: userType === 'student' ? 4 : 'calc(50% - 5px)',
                            width: 'calc(52% - 8px)',
                            height: 'calc(100% - 8px)',
                            backgroundColor: '#288af3',
                            borderRadius: 14,
                            transition: 'left 0.3s ease'
                        }}
                    />
                    <button
                        type="button"
                        onClick={() => setUserType('student')}
                        disabled={loading}
                        style={{
                            flex: 1,
                            zIndex: 1,
                            background: 'none',
                            border: 'none',
                            color: userType === 'student' ? '#fff' : '#000',
                            fontWeight: 'bold',
                            cursor: loading ? 'not-allowed' : 'pointer',
                            outline: 'none'
                        }}
                    >
                        Student
                    </button>
                    <button
                        type="button"
                        onClick={() => setUserType('org')}
                        disabled={loading}
                        style={{
                            flex: 1,
                            zIndex: 1,
                            background: 'none',
                            border: 'none',
                            color: userType === 'org' ? '#fff' : '#000',
                            fontWeight: 'bold',
                            cursor: loading ? 'not-allowed' : 'pointer',
                            outline: 'none'
                        }}
                    >
                        Organization
                    </button>
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
                    {loading ? 'Creating Account...' : 'Create Account'}
                </button>
            </form>
            <button
                style={{
                    marginTop: 15,
                    width: '100%',
                    padding: '10px',
                    backgroundColor: '#288af3',
                    color: 'white',
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
