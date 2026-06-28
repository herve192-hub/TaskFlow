// AppRoutes.tsx
import { BrowserRouter, Routes, Route } from "react-router-dom";

import Signup from "../pages/Signup";
import Login from "../pages/Login";
import ForgotPassword from "../pages/ForgotPassword";
// import Dashboard from "../dashboard/Dashboard";
import DashboardLayout from "../components/DashboardLayout";
import ProtectedRoute from "./protectedRoute";
import Dashboard from "../components/dashboard/Dashboard";

export default function AppRoutes() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Login />} />
                <Route path="/signup" element={<Signup />} />
                <Route path="/login" element={<Login />} />
                <Route path="/forgot-password" element={<ForgotPassword />}/>
                <Route
                    path="/dashboard"
                    element={
                        <ProtectedRoute>
                            <DashboardLayout />
                        </ProtectedRoute>
                    }
                >
                    <Route index element={<Dashboard />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
}