/**
 * Browser entry point: mount one React root with StrictMode, the shared MUI theme,
 * CSS reset, and global toast notifications. App owns routing and error recovery.
 * StrictMode may replay effects in development; network effects must tolerate cleanup.
 */

import React from 'react';
import ReactDOM from 'react-dom/client';
// AppRoutes owns the router; keep theme, baseline styles, and notifications shared by all routes.
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

import App from './App.tsx';

import {ThemeProvider} from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";

import { theme } from "./theme/theme.ts";


ReactDOM.createRoot(  document.getElementById('root')!  ).render(

    <React.StrictMode>
      <ThemeProvider theme={theme}>
          <CssBaseline />
          <App />

          <ToastContainer
            position="top-right"
            autoClose={3000}
            hideProgressBar={false}
            newestOnTop
            pauseOnHover
            draggable
          />
      </ThemeProvider>
    </React.StrictMode>,
);
