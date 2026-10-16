// 
import { Box, Typography } from "@mui/material";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import TaskFlowLogo from "./TaskFlowLogo";

{/* this interface defines the props for the AuthLayout component, 
 which expects a single prop called "children" of type ReactNode. 
 This allows the component to accept any valid React elements as its children, 
 enabling flexible composition of UI elements within the layout.*/}
interface Props {
  children: ReactNode;
}

export default function AuthLayout({ children }: Props) {
  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        bgcolor: "#F8FAFC",
      }}
    >
      {/* Left Side */}
      <Box
        sx={{
          flex: 1,
          background:
            "linear-gradient(135deg, #EFF6FF 0%, #ECFEFF 55%, #F0FDFA 100%)",
          color: "text.primary",
          display: { xs: "none", md: "flex" },
          flexDirection: "column",
          justifyContent: "center",
          px: 8,
        }}
      >
        <Box
          component={Link}
          to="/"
          aria-label="TaskFlow home"
          sx={{ display: "block", width: "fit-content", mb: 3, borderRadius: 2 }}
        >
          <TaskFlowLogo variant="stacked" />
        </Box>

        <Typography variant="h6" sx={{ mb: 5 }}>
          Manage projects, track progress, and collaborate
          efficiently from anywhere.
        </Typography>

        <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
          <Typography>✓ Project Management</Typography>
          <Typography>✓ Kanban Boards</Typography>
          <Typography>✓ Team Collaboration</Typography>
          <Typography>✓ Real-Time Updates</Typography>
        </Box>
      </Box>

      {/* Right Side */}
      <Box
        sx={{
          flex: 1,
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
          p: { xs: 2, sm: 3 },
        }}
      >
        <Box
          component={Link}
          to="/"
          aria-label="TaskFlow home"
          sx={{ display: { xs: "block", md: "none" }, mb: 3, borderRadius: 2 }}
        >
          <TaskFlowLogo width={220} />
        </Box>
        {children}
      </Box>
    </Box>
  );
}
