{/* */}
import { Box, Typography } from "@mui/material";
import TaskAltIcon from "@mui/icons-material/TaskAlt";
import { ReactNode } from "react";

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
            "linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%)",
          color: "white",
          display: { xs: "none", md: "flex" },
          flexDirection: "column",
          justifyContent: "center",
          px: 8,
        }}
      >
        <TaskAltIcon sx={{ fontSize: 60, mb: 2 }} />

        <Typography variant="h3" sx={{ fontWeight: "bold", mb: 2 }}>
          TaskFlow
        </Typography>

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
          justifyContent: "center",
          alignItems: "center",
          p: 3,
        }}
      >
        {children}
      </Box>
    </Box>
  );
}