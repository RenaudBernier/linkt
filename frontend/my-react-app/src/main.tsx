import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

import './index.css'
import App from './App.tsx'
import { BrowserRouter } from 'react-router-dom'
import Header from "./components/Header.tsx";
import { Toolbar } from "@mui/material";
import { AuthProvider } from './contexts/AuthContext.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
      <BrowserRouter>
          <AuthProvider>
              <Header />
              <Toolbar />
              <App />
          </AuthProvider>
      </BrowserRouter>
  </StrictMode>,
)
