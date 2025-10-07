import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
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
import './App.css'

function App() {
  const [count, setCount] = useState(0)

  return (
    <>
      
      <div>
        <Box component = "section" sx = {{p: 2, width: '100%', bgcolor: '#008dd5', color: 'white', border: '5px white'}}>
          <h1> Linkdt </h1>
          <p> Student Management System! </p>
        </Box>
      </div>
      <div>
        <Box component= "section" sx = {{p: 2, width: '100%', bgcolor: '#373f51', color: 'white', border: '5px white'}}>
          <a href="https://vite.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
        </Box>
      </div>
      <h1>Vite + React</h1>
      <div className="card">
        <button onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </button>
        <p>
          Edit <code>src/App.tsx</code> and save to test HMR
        </p>
      </div>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <br></br>
      <p className="read-the-docs">
        Click on the Vite and React logos to learn more
      </p>
    </>
  )
}

export default App
