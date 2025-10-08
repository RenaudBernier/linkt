// src/App.tsx
import { Routes, Route, useNavigate } from 'react-router-dom';
import './App.css';
import SignUp from './SignUp';
import Login from './Login';
import CheckoutPage from "./components/CheckoutPage.tsx";

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

                <button onClick={() => navigate('/checkoutpage')}>
                    Go to Checkout
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
            <Route path="/checkoutpage" element={<CheckoutPage/>}/>
        </Routes>
    );
}

export default App;