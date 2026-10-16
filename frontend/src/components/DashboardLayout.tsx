// DashboardLayout.tsx ...
import { Box, Toolbar, useMediaQuery, useTheme } from "@mui/material";
import { useState } from "react";
import { Outlet } from "react-router-dom";

import Sidebar from "./dashboard/Sidebar";
import Topbar from "./dashboard/Topbar";

/** Persistent dashboard chrome; nested routes render inside Outlet below the fixed toolbar. */
export default function DashboardLayout() {
  const theme = useTheme();
  const isSmallScreen = useMediaQuery(theme.breakpoints.down("md"));
  const [collapsed, setCollapsed] = useState(false);
  // Small screens force the compact drawer while preserving the user's desktop toggle state.
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
