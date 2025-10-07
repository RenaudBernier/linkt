import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './contexts/AuthContext';

export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();
    const { login } = useAuth();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        // Dummy implementation - simulate login
        await new Promise(resolve => setTimeout(resolve, 500));

        // For now, accept any credentials and log the user in
        login({
            firstName: 'Test',
            lastName: 'User',
            email: email,
            phoneNumber: ''
        });

        navigate('/');
    };

    return (
        <div style={{maxWidth: 400, margin: 'auto', padding: 20}}>
            <h2>Log In</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Email:</label><br/>
                    <input
                        type="email"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        required
                    />
                </div>
                <div style={{marginTop: 10}}>
                    <label>Password:</label><br/>
                    <input
                        type="password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        required
                    />
                </div>
                <button type="submit" style={{marginTop: 20}}>Log In</button>
            </form>
            <button style={{marginTop: 20}} onClick={() => navigate('/')}>
                Back To Home
            </button>
        </div>
    );
}
