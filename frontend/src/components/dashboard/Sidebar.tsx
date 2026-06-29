// Sidebar.tsx ...
import { Box, Drawer, IconButton, List, ListItemButton, ListItemIcon, ListItemText, Toolbar } from "@mui/material";
import DashboardIcon from "@mui/icons-material/Dashboard";
import TaskIcon from "@mui/icons-material/Task";
import FolderIcon from "@mui/icons-material/Folder";
import GroupIcon from "@mui/icons-material/Group";
import SettingsIcon from "@mui/icons-material/Settings";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";

import { collapsedDrawerWidth, drawerWidth } from "./layoutConstants";

type SidebarProps = {
  selected?: string;
  collapsed: boolean;
  onToggle: () => void;
};

const navigationItems = [
  { label: "Dashboard", icon: <DashboardIcon /> },
  { label: "Tasks", icon: <TaskIcon /> },
  { label: "Projects", icon: <FolderIcon /> },
  { label: "Teams", icon: <GroupIcon /> },
  { label: "Settings", icon: <SettingsIcon /> },
];

export default function Sidebar({ selected = "Dashboard", collapsed, onToggle }: SidebarProps) {
  const width = collapsed ? collapsedDrawerWidth : drawerWidth;

  return (
    <Drawer
      variant="permanent"
      sx={{
        width,
        flexShrink: 0,
        "& .MuiDrawer-paper": {
          width,
          boxSizing: "border-box",
          transition: "width 0.2s ease-in-out",
          overflowX: "hidden",
        },
      }}
    >
      <Toolbar />
      <Box sx={{ display: "flex", justifyContent: collapsed ? "center" : "flex-end", px: 1, py: 1 }}>
        <IconButton onClick={onToggle} size="small">
          {collapsed ? <ChevronRightIcon /> : <ChevronLeftIcon />}
        </IconButton>
      </Box>
      <List sx={{ pt: 1 }}>
        {navigationItems.map((item) => (
          <ListItemButton
            key={item.label}
            selected={selected === item.label}
            sx={{
              minHeight: 48,
              justifyContent: collapsed ? "center" : "flex-start",
              px: collapsed ? 1.5 : 2,
            }}
          >
            <ListItemIcon
              sx={{
                minWidth: collapsed ? 0 : 36,
                mr: collapsed ? 0 : 1.5,
                justifyContent: "center",
              }}
            >
              {item.icon}
            </ListItemIcon>
            {!collapsed && <ListItemText primary={item.label} />}
          </ListItemButton>
        ))}
      </List>
    </Drawer>
  );
}
