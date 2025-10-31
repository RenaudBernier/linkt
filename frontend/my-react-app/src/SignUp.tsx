import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { signUp } from './api/users.api';
import { useAuth } from './contexts/AuthContext';

export default function SignUp() {
    const [name, setName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [password, setPassword] = useState('');
    const [userType, setUserType] = useState<'student' | 'organizer'>('student');
    const navigate = useNavigate();
    const { login } = useAuth();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        // Log the request data
        const signUpData = {
            firstName: name,
            lastName: lastName,
            email: email,
            password: password,
            phoneNumber: phoneNumber,
            userType: userType,
            organizationName: userType === 'organizer' ? 'OrgName' : undefined
        };
        console.log('Sending signup request with data:', signUpData);

        try {
            const signUpData = {
                firstName: name,
                lastName: lastName,
                email: email,
                password: password,
                phoneNumber: phoneNumber,
                userType: userType,
                organizationName: userType === 'organizer' ? 'OrgName' : undefined
            };
            
            console.log('Sending signup request with data:', signUpData);
            
            const response = await signUp(signUpData);

            // Log successful response
            console.log('Signup response:', response);

            // Log the user in with token and redirect to homepage
            login({
                firstName: response.firstName,
                lastName: response.lastName,
                email: response.email,
                phoneNumber: response.phoneNumber || '',
                userType: response.userType
            }, response.token);

            navigate('/');
        } catch (error) {
            if (axios.isAxiosError(error)) {
                console.error('Signup failed!', {
                    status: error.response?.status,
                    statusText: error.response?.statusText,
                    data: error.response?.data,
                    message: error.message
                });
            } else {
                console.error('Signup failed!', error);
            }
            // TODO: Show error message to user
        }
    };

    return (
        <div style={{maxWidth: 400, margin: 'auto', padding: 20}}>
            <h2>Sign Up</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Name:</label><br/>
                    <input
                        type="text"
                        value={name}
                        onChange={e => setName(e.target.value)}
                        required
                    />
                </div>
                <div style={{marginTop: 10}}>
                    <label>Last Name:</label><br/>
                    <input
                        type="text"
                        value={lastName}
                        onChange={e => setLastName(e.target.value)}
                        required
                    />
                </div>
                <div style={{marginTop: 10}}>
                    <label>Email:</label><br/>
                    <input
                        type="email"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        required
                    />
                </div>
                <div style={{marginTop: 10}}>
                    <label>Phone Number:</label><br/>
                    <input
                        type="tel"
                        value={phoneNumber}
                        onChange={e => setPhoneNumber(e.target.value)}
                        required
                    />
                </div>
                <div style={{marginTop: 10, marginBottom: 20}}>
                    <label>Password:</label><br/>
                    <input
                        type="password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        required
                    />
                </div>
                <div style={{
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
                }}>
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
                        style={{
                            flex: 1,
                            zIndex: 1,
                            background: 'none',
                            border: 'none',
                            color: userType === 'student' ? '#fff' : '#000',
                            fontWeight: 'bold',
                            cursor: 'pointer',
                            outline: 'none'
                        }}
                    >
                        Student
                    </button>
                    <button
                        type="button"
                        onClick={() => setUserType('organizer')}
                        style={{
                            flex: 1,
                            zIndex: 1,
                            background: 'none',
                            border: 'none',
                            color: userType === 'organizer' ? '#fff' : '#000',
                            fontWeight: 'bold',
                            cursor: 'pointer',
                            outline: 'none'
                        }}
                    >
                        Organization
                    </button>
                </div>
                <button type="submit" style={{marginTop: 20}}>Create Account</button>
            </form>
            <button style={{marginTop: 20}} onClick={() => navigate('/')}>
                Back To Home
            </button>
        </div>

    );


}