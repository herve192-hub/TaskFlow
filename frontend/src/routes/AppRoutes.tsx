import {
  BrowserRouter,
  Route,
  Routes,
  Navigate,
} from "react-router-dom";

import Signup from "../pages/Signup";
import Login from "../pages/Login";
import ForgotPassword from "../pages/ForgotPassword";
import Projects from "../pages/Projects";
import Tasks from "../pages/Tasks";
import ProjectTasks from "../pages/ProjectTasks";
import ProjectMembers from "../pages/ProjectMembers";
import Teams from "../pages/Teams";

import DashboardLayout from "../components/DashboardLayout";
import Dashboard from "../components/dashboard/Dashboard";

import ProtectedRoute from "./protectedRoute";

/**
 * Own the browser router and nested dashboard pages. The build flag hides public signup;
 * backend approval and authorization remain enforced independently on every request.
 */
export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={import.meta.env.VITE_PUBLIC_REGISTRATION_ENABLED === 'false'
          ? <Navigate to="/login" replace /> : <Signup />} />
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
          <Route path="teams" element={<Teams />} />
          <Route path="projects/:projectId/tasks" element={<ProjectTasks />} />
          <Route path="projects/:projectId/members" element={<ProjectMembers />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
