// AppRoutes.tsx
import { BrowserRouter, Routes, Route } from "react-router-dom";

import Signup from "../pages/Signup";
import Login from "../pages/Login";
import ForgotPassword from "../pages/ForgotPassword";
// import Dashboard from "../pages/Dashboard"; 

export default function AppRoutes() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Login />} />
                <Route path="/signup" element={<Signup />} />
                <Route path="/login" element={<Login />} />
                <Route path="/forgot-password" element={<ForgotPassword />}/>
                {/* <Route path="/dashboard" element={<Dashboard />} /> */}
            </Routes>
        </BrowserRouter>
    );
}