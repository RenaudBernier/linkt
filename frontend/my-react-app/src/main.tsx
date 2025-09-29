import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import Header from "./components/Header.tsx";
import "./index.css";
import App from "./App.tsx";
import { Toolbar } from "@mui/material";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <Header />
    <Toolbar />
    <App />
  </StrictMode>
);
