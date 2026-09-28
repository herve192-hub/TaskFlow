import {
  BrowserRouter,
  Route,
  Routes,
} from "react-router-dom";

import Signup from "../pages/Signup";
import Login from "../pages/Login";
import ForgotPassword from "../pages/ForgotPassword";
import Projects from "../pages/Projects";
import Tasks from "../pages/Tasks";
import ProjectTasks from "../pages/ProjectTasks";

import DashboardLayout from "../components/DashboardLayout";
import Dashboard from "../components/dashboard/Dashboard";

import ProtectedRoute from "./protectedRoute";

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route
          path="/forgot-password"
          element={<ForgotPassword />}
        />

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Dashboard />} />
          <Route path="projects" element={<Projects />} />
          <Route path="tasks" element={<Tasks />} />
          <Route path="projects/:projectId/tasks" element={<ProjectTasks />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}