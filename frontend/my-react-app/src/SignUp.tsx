import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function SignUp() {
    const [name, setName] = useState('');
    //const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        console.log('Signing up with:', { name, email, password });
    };
    const navigate = useNavigate();

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
                <button type="submit" style={{marginTop: 20}}>Create Account</button>
            </form>
            <button style={{marginTop: 20}} onClick={() => navigate('/')} >
                Back To Home
            </button>
        </div>

    );


}