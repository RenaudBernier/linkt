import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { signUp } from './api/users.api';
import { useAuth } from './contexts/AuthContext';
import { Toolbar, Box, Typography } from "@mui/material";
import './SignUp.css';

export default function SignUp() {
    const [name, setName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [password, setPassword] = useState('');
    const [userType, setUserType] = useState<'student' | 'org'>('student');
    const [organizationName, setOrganizationName] = useState('');
    const navigate = useNavigate();
    const { login } = useAuth();

    useEffect(() => {
        document.body.classList.add('signup-page-background');
        return () => {
            document.body.classList.remove('signup-page-background');
        };
    }, []);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (password.length < 7) {
            alert('ERROR: Password must be at least 7 characters long. Please enter a longer password.'); //setError does not work here, make it an alert!
            return;
        }

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
            console.error('Signup failed!', error);
            // TODO: Show error message to user
        }
    };

    return (
        <div className="signup-container">
                <h2 className="signuptitle">SIGN UP</h2>
                <form onSubmit={handleSubmit} className="signup-form">
                <div>
                    <label>Name:</label>
                    <input
                        type="text"
                        value={name}
                        onChange={e => setName(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>Last Name:</label>
                    <input
                        type="text"
                        value={lastName}
                        onChange={e => setLastName(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>Email:</label>
                    <input
                        type="email"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>Phone Number:</label>
                    <input
                        type="tel"
                        value={phoneNumber}
                        onChange={e => setPhoneNumber(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>Password:</label>
                    <label className="password-note"> (Must be longer than 7 characters!) </label>
                    <input
                        type="password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        required
                    />
                </div>
                {userType === 'org' && (
                    <div>
                        <label>Organization Name:</label>
                        <input
                            type="text"
                            value={organizationName}
                            onChange={e => setOrganizationName(e.target.value)}
                            required
                        />
                    </div>
                )}
                <div className="user-type-toggle">
                    <div className={`user-type-slider ${userType}`} />
                    <button
                        type="button"
                        onClick={() => setUserType('student')}
                        className={`user-type-button ${userType === 'student' ? 'active' : ''}`}
                    >
                        Student
                    </button>
                    <button
                        type="button"
                        onClick={() => setUserType('org')}
                        className={`user-type-button ${userType === 'org' ? 'active' : ''}`}
                    >
                        Organization
                    </button>
                </div>
                <button type="submit" className="submit-button">Create Account</button>
            </form>
            <button className="back-button" onClick={() => navigate('/')}>
                Back To Home
            </button>
        </div>
    );
}
