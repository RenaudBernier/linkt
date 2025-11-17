import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './contexts/AuthContext';
import authService from './services/authService';


export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();
    const { login } = useAuth();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        try {
            const response = await authService.login({ email, password });

            console.log('Login response:', response);
            console.log('User type from response:', response.userType);

            const userData = {
                firstName: response.firstName,
                lastName: response.lastName,
                email: response.email,
                phoneNumber: response.phoneNumber,
                userType: response.userType
            };

            console.log('User data being passed to login():', userData);

            // Update auth context with user data and token
            login(userData, response.token);

            console.log('After login, localStorage user:', localStorage.getItem('user'));

            navigate('/');
        } catch (err) {
            console.error('Login error:', err);
            setError('Login failed. Please check your credentials.');
        }
    };

    return (
        <div style = {{display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh'}}>
        <div className = 'loginDiv' style={{maxWidth: 500, fontSize: '25px', margin: 'auto', padding: 20, border: '1px solid #ccc', borderRadius: 8, boxShadow: '0 2px 4px rgba(0,0,0,0.1)', backgroundColor: '#a63a50'}}>
            <h2 style = {{fontSize: '60px', fontFamily: 'Montserrat', fontStyle: 'italic', color: 'white'}}>Log In</h2>
            {error && <div style={{color: 'red', marginBottom: 10}}>{error}</div>}
            <form onSubmit={handleSubmit}>
                <div style = {{fontSize: '25px', height: '100px'}}>
                    <label>Email:</label><br/>
                    <input
                        type="email"
                        size={40}
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>Password:</label><br/>
                    <input
                        type="password"
                        size={40}
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
        </div>
    );
}
