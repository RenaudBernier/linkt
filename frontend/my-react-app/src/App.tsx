// src/App.tsx
import {Routes, Route, useNavigate, Outlet} from 'react-router-dom';
import './App.css';
import SignUp from './SignUp';
import Login from './Login';
import CheckoutPage from "./components/CheckoutPage.tsx";
import Header from "./components/Header.tsx";
import {Toolbar} from "@mui/material";

function MainLayout() {
    return (
        <>
            <Header/>
            <Toolbar/>
            <Outlet />
        </>
    );
}

function BlankLayout() {
    return (
        <main>
            <Outlet />
        </main>
    );
}


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
                <Route element={<MainLayout />}>
                    <Route path="/" element={<Home />} />
                </Route>

                <Route element={<BlankLayout/>}>
                    <Route path="/login" element={<Login/>}/>
                    <Route path="/signup" element={<SignUp/>}/>
                    <Route path="/checkoutpage" element={<CheckoutPage/>}/>
                </Route>
            </Routes>
    );
}

export default App;