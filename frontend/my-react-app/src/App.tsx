// src/App.tsx
import { Routes, Route, useNavigate } from 'react-router-dom';
import './App.css';
import SignUp from './SignUp';
import Login from './Login';

function Home() {
    const navigate = useNavigate();

    return (
        <>
            <div className="card">
                <button onClick={() => navigate('/signup')}>
                    Go to Sign Up
                </button>
                <button onClick={() => navigate('/login')}>
                    Go to Login
                </button>
            </div>
        </>
    );
}

function App() {
    return (
        <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/signup" element={<SignUp />} />
            <Route path="/login" element={<Login />} />
        </Routes>
    );
}

export default App;