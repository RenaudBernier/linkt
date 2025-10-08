// src/App.tsx
import { useState } from 'react';
import { Routes, Route, useNavigate } from 'react-router-dom';
import reactLogo from './assets/react.svg';
import viteLogo from '/vite.svg';
import {
  AppBar,
  Toolbar,
  Box,
  TextField,
  InputAdornment,
  IconButton,
  Menu,
  MenuItem,
  Avatar,
} from "@mui/material";
import './App.css';
import SignUp from './SignUp';
import Login from './Login';

function Home() {
    const [count, setCount] = useState(0);
    const navigate = useNavigate();

    return (
        <>
            
      <Box component = "section" sx = {{p: 2, width: '100%', bgcolor: '#008dd5', color: 'white', border: '5px white'}}>
        <h1> Linkdt </h1>
        <p> Welcome to our comprehensive campus Events & Ticketing service, designed to streamline event management and boost student engagement for students! 
          We allow students to easily discover and search for events using comprehensive filters, save them to their personal calendar, and claim digital, QR-coded
          tickets (free or mock paid) for check-in! If you are an organizer, then you are welcome too! Organizers benefit from the ability to create, manage, 
          and track attendance for their events. They can also gain valuable insights for their events via our analytics dashboards. We are also welcoming 
          campus administrators, who can oversee organizations and moderate all content. Linkt is our brand-new system that connects students with campus life 
          while providing essential tools for hosting and administrating events! </p>
      </Box>

      
      <Box component = "section" sx = {{p: 2, width: '100%', bgcolor: '#373f51', color: 'white', border: '5px white'}}>
        {/* 
        Former Logo
              <a href="https://vite.dev" target="_blank">
                <img src={viteLogo} className="logo" alt="Vite logo" />
            </a>
            <a href="https://react.dev" target="_blank">
                <img src={reactLogo} className="logo react" alt="React logo" />
      </a>
        */}
      <h1> Top Events </h1>
      <h3> Backend for this hasn't been implemented yet! </h3>
      <img src = "src\images\samantha-gades-fIHozNWfcvs-unsplash.jpg" alt = "neat college photo!" style = {{maxWidth: '33%', maxHeight: '33%'}}></img>
      <h2> Frosh Night </h2>
      <p> New to school and don't know where to start? Have some drinks, play games and meet some new people at the school's frosh night! </p>
      <br></br>
      <img src = "src\images\swag-slayer-dd2EOQBycJY-unsplash.jpg"  alt = "neat college photo!" style = {{maxWidth: '33%', maxHeight: '33%'}}></img>
      <h2> DJ Night </h2>
      <p> The EDM Club is organizing an all-night dance festival on the 22nd of October! Click for more details! </p>
      </Box>


      <Box component = "section" sx = {{p: 2, width: '100%', bgcolor: '#a63a50', color: 'white', border: '5px white'}}>
        <h2> Ready To Interact? </h2>
        {/*
        <button onClick={() => setCount((count) => count + 1)}>
                    count is {count}
                </button>
        <p>
                    Edit <code>src/App.tsx</code> and save to test HMR
                </p>
        */}
                <button onClick={() => navigate('/signup')}>
                    Go to Sign Up
                </button>
                <button onClick={() => navigate('/login')}>
                    Go to Login
                </button>
      </Box>
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