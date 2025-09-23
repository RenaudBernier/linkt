import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import Header from './components/Header.tsx'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <div style = {{minHeight: '0vh', width: '100%'}}>
    <Header />
    <App />
    </div>
  </StrictMode>,
)
