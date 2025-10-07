import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import Header from "./components/Header.tsx";
import "./index.css";

import HomePage from "./HomePage.tsx";
import { Toolbar } from "@mui/material";

import {createBrowserRouter, RouterProvider} from 'react-router-dom'
import CheckoutPage from "./components/CheckoutPage.tsx";

const router = createBrowserRouter([
    {path: '/',element:<HomePage/>},
    {path: '/checkoutpage',element:<CheckoutPage/>}
]);

createRoot(document.getElementById("root")!).render(
  <StrictMode>
   <RouterProvider router={router} />
  </StrictMode>
);
