/**
 * Route map for public authentication and nested dashboard screens. Lazy imports
 * split page bundles; Suspense handles loading and App catches render failures.
 * ProtectedRoute observes session display metadata; backend cookies authorize APIs.
 * VITE_PUBLIC_REGISTRATION_ENABLED is a build-time UI switch; the auth service's
 * production policy independently controls who may create accounts.
 */

import {
  BrowserRouter,
  Route,
  Routes,
  Navigate,
} from "react-router-dom";

import { lazy, Suspense } from "react";
import { Box, CircularProgress } from "@mui/material";

const Signup = lazy(() => import("../pages/Signup"));
const Login = lazy(() => import("../pages/Login"));
const ForgotPassword = lazy(() => import("../pages/ForgotPassword"));
const Projects = lazy(() => import("../pages/Projects"));
const Tasks = lazy(() => import("../pages/Tasks"));
const ProjectTasks = lazy(() => import("../pages/ProjectTasks"));
const ProjectMembers = lazy(() => import("../pages/ProjectMembers"));
const Teams = lazy(() => import("../pages/Teams"));
const Profile = lazy(() => import("../pages/Profile"));
const Settings = lazy(() => import("../pages/Settings"));
const DashboardLayout = lazy(() => import("../components/DashboardLayout"));
const Dashboard = lazy(() => import("../components/dashboard/Dashboard"));

import ProtectedRoute from "./protectedRoute";

/**
 * Own the browser router and nested dashboard pages. The build flag hides public signup;
 * backend approval and authorization remain enforced independently on every request.
 */
export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Suspense fallback={<Box sx={{ display: "flex", justifyContent: "center", py: 8 }}>
        <CircularProgress aria-label="Loading page" />
      </Box>}>
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
            <Route path="profile" element={<Profile />} />
            <Route path="settings" element={<Settings />} />
            <Route path="projects/:projectId/tasks" element={<ProjectTasks />} />
            <Route path="projects/:projectId/members" element={<ProjectMembers />} />
          </Route>
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}
