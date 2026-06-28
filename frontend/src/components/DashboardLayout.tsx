// DashboardLayout.tsx ...

import { Box, Toolbar } from "@mui/material";
import { Outlet } from "react-router-dom";

import Sidebar from "./dashboard/Sidebar";
import Topbar from "./dashboard/Topbar";

export default function DashboardLayout() {
  return (
    <Box sx={{ display: "flex" }}>
      <Topbar />
      <Sidebar />

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          px: { xs: 2, sm: 3, md: 4 },
          mx: { xs: 1, sm: 2 },
          overflowX: "hidden",
        }}
      >
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
}
