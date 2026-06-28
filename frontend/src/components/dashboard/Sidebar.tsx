import { Drawer, List, ListItemButton, ListItemIcon, ListItemText, Toolbar } from "@mui/material";
import DashboardIcon from "@mui/icons-material/Dashboard";
import TaskIcon from "@mui/icons-material/Task";
import FolderIcon from "@mui/icons-material/Folder";
import GroupIcon from "@mui/icons-material/Group";
import SettingsIcon from "@mui/icons-material/Settings";

import { drawerWidth } from "./layoutConstants";

type SidebarProps = {
  selected?: string;
};

const navigationItems = [
  { label: "Dashboard", icon: <DashboardIcon /> },
  { label: "Tasks", icon: <TaskIcon /> },
  { label: "Projects", icon: <FolderIcon /> },
  { label: "Teams", icon: <GroupIcon /> },
  { label: "Settings", icon: <SettingsIcon /> },
];

export default function Sidebar({ selected = "Dashboard" }: SidebarProps) {
  return (
    <Drawer
      variant="permanent"
      sx={{
        width: drawerWidth,
        flexShrink: 0,
        "& .MuiDrawer-paper": {
          width: drawerWidth,
          boxSizing: "border-box",
        },
      }}
    >
      <Toolbar />
      <List sx={{ pt: 4 }}>
        {navigationItems.map((item) => (
          <ListItemButton key={item.label} selected={selected === item.label}>
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primary={item.label} />
          </ListItemButton>
        ))}
      </List>
    </Drawer>
  );
}
