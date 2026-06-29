// DashboardLayout.tsx ...
import { Box, Toolbar, useMediaQuery, useTheme } from "@mui/material";
import { useState } from "react";
import { Outlet } from "react-router-dom";

import Sidebar from "./dashboard/Sidebar";
import Topbar from "./dashboard/Topbar";

export default function DashboardLayout() {
  const theme = useTheme();
  const isSmallScreen = useMediaQuery(theme.breakpoints.down("md"));
  const [collapsed, setCollapsed] = useState(false);
  const isCollapsed = isSmallScreen || collapsed;

  return (
    <Box sx={{ display: "flex" }}>
      <Topbar collapsed={isCollapsed} />
      <Sidebar collapsed={isCollapsed} onToggle={() => setCollapsed((prev) => !prev)} />

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
