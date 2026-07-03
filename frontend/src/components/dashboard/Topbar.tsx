// Topbar.tsx ...
import {
  AppBar,
  Avatar,
  Box,
  IconButton,
  InputBase,
  Paper,
  Popper,
  ClickAwayListener,
  Badge,
  List,
  ListItem,
  ListItemText,
  Divider,
  Menu,
  MenuItem,
  Toolbar,
  Typography,
  ListItemButton,
} from "@mui/material";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import SearchIcon from "@mui/icons-material/Search";
import NotificationsIcon from "@mui/icons-material/Notifications";

import { collapsedDrawerWidth, drawerWidth } from "./layoutConstants";

type TopbarProps = {
  collapsed: boolean;
};

export default function Topbar({ collapsed }: TopbarProps) {
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchAnchor, setSearchAnchor] = useState<null | HTMLElement>(null);

  const [notifAnchor, setNotifAnchor] = useState<null | HTMLElement>(null);
  const notifOpen = Boolean(notifAnchor);

  const [notifications, setNotifications] = useState(
    [
      { id: 1, title: "Task overdue", body: "Project A task is overdue.", unread: true },
      { id: 2, title: "New comment", body: "You have a new comment on Task 4.", unread: true },
      { id: 3, title: "Deployment", body: "Deployment succeeded.", unread: false },
    ] as { id: number; title: string; body: string; unread: boolean }[]
  );

  const handleLogout = () => {
    localStorage.clear();
    navigate("/");
  };

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  return (
    <AppBar
      position="fixed"
      sx={{
        zIndex: (theme) => theme.zIndex.drawer + 1,
        ml: { xs: 0, sm: collapsed ? `${collapsedDrawerWidth}px` : `${drawerWidth}px` },
        px: { xs: 1, sm: 2 },
        backgroundColor: "background.paper",
        color: "text.primary",
        boxShadow: "0px 2px 10px rgba(15, 23, 42, 0.08)",
        borderBottom: "1px solid",
        borderColor: "divider",
      }}
    >
      <Toolbar sx={{ justifyContent: "space-between", minHeight: { xs: 64, sm: 72 } }}>
        <Box sx={{ display: "flex", alignItems: "center", gap: 1.5 }}>
          <Box
            sx={{
              width: 36,
              height: 36,
              borderRadius: "10px",
              bgcolor: "primary.main",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              color: "white",
              fontWeight: 700,
            }}
          >
            T
          </Box>
          <Typography
            variant="h6"
            sx={{
              fontWeight: 700,
              letterSpacing: 0.3,
              display: { xs: "none", sm: "block" },
            }}
          >
            TaskFlow
          </Typography>
        </Box>

        {/* Centered search area */}
        <Box sx={{ flex: 1, display: "flex", justifyContent: "center", alignItems: "center" }}>
          <Box sx={{ display: "flex", alignItems: "center" }}>
            <IconButton
              onClick={(e) => setSearchAnchor((a) => (a ? null : e.currentTarget))}
              sx={{
                border: "1px solid",
                borderColor: "divider",
                bgcolor: "grey.50",
                "&:hover": { bgcolor: "grey.100" },
                display: { xs: "inline-flex", sm: "none" },
              }}
            >
              <SearchIcon />
            </IconButton>

            <Box sx={{ display: { xs: "none", sm: "block" }, ml: 1, width: 320 }}>
              <Paper
                component="form"
                onSubmit={(e) => e.preventDefault()}
                sx={{ display: "flex", alignItems: "center", px: 1, py: 0.3 }}
              >
                <SearchIcon color="action" />
                <InputBase
                  placeholder="Search tasks, projects, people..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  sx={{ ml: 1, flex: 1 }}
                />
              </Paper>
              {searchQuery && (
                <Paper sx={{ mt: 0.5, maxHeight: 240, overflow: "auto" }}>
                  <List dense>
                    {["Create task", "Project A", "Haris"].
                      filter((s) => s.toLowerCase().includes(searchQuery.toLowerCase()))
                      .map((s, i) => (
                        <ListItem key={i} disablePadding>
                          <ListItemButton onClick={() => setSearchQuery(s)}>
                            <ListItemText primary={s} />
                          </ListItemButton>
                        </ListItem>
                      ))}
                  </List>
                </Paper>
              )}
            </Box>

            <Popper open={Boolean(searchAnchor)} anchorEl={searchAnchor} placement="bottom-start">
              <ClickAwayListener onClickAway={() => setSearchAnchor(null)}>
                <Paper sx={{ p: 1, mt: 1, width: 260 }}>
                  <Paper
                    component="form"
                    onSubmit={(e) => e.preventDefault()}
                    sx={{ display: "flex", alignItems: "center", px: 1, py: 0.3 }}
                  >
                    <SearchIcon color="action" />
                    <InputBase
                      placeholder="Search tasks, projects, people..."
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      sx={{ ml: 1, flex: 1 }}
                      autoFocus
                    />
                  </Paper>
                  {searchQuery && (
                    <List dense sx={{ mt: 1, maxHeight: 200, overflow: "auto" }}>
                      {["Create task", "Project A", "Haris"].
                        filter((s) => s.toLowerCase().includes(searchQuery.toLowerCase()))
                        .map((s, i) => (
                          <ListItem key={i} disablePadding>
                            <ListItemButton onClick={() => setSearchQuery(s)}>
                              <ListItemText primary={s} />
                            </ListItemButton>
                        </ListItem>
                        ))}
                    </List>
                  )}
                </Paper>
              </ClickAwayListener>
            </Popper>
          </Box>
        </Box>
        <Box sx={{ display: "flex", alignItems: "center", gap: { xs: 0.8, sm: 1.2 } }}>
          <IconButton
            onClick={(e) => setNotifAnchor(e.currentTarget)}
            sx={{
              border: "1px solid",
              borderColor: "divider",
              bgcolor: "grey.50",
              "&:hover": { bgcolor: "grey.100" },
            }}
          >
            <Badge color="error" badgeContent={notifications.filter((n) => n.unread).length}>
              <NotificationsIcon />
            </Badge>
          </IconButton>

          <Menu
            anchorEl={notifAnchor}
            open={notifOpen}
            onClose={() => setNotifAnchor(null)}
            slotProps={{ paper: { sx: { mt: 1.6, minWidth: 320 } } }}
          >
            <Box sx={{ px: 2, py: 1 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                Notifications
              </Typography>
            </Box>
            <Divider />
            {notifications.map((n) => (
              <MenuItem
                key={n.id}
                onClick={() => {
                  setNotifications((prev) => prev.map((p) => (p.id === n.id ? { ...p, unread: false } : p)));
                }}
                sx={{ whiteSpace: "normal" }}
              >
                <ListItemText primary={n.title} secondary={n.body} />
              </MenuItem>
            ))}
            <Divider />
            <MenuItem onClick={() => { setNotifications([]); setNotifAnchor(null); }} sx={{ justifyContent: "center" }}>
              Clear all
            </MenuItem>
          </Menu>

          <Avatar
            onClick={handleClick}
            sx={{
              bgcolor: "primary.main",
              cursor: "pointer",
              width: { xs: 34, sm: 40 },
              height: { xs: 34, sm: 40 },
              border: "2px solid",
              borderColor: "primary.light",
            }}
          >
            H
          </Avatar>

          <Menu
            anchorEl={anchorEl}
            open={open}
            onClose={handleClose}
            slotProps={{
              paper: {
                sx: {
                  mt: 1.6,
                  borderRadius: 2,
                  boxShadow: "0px 16px 40px rgba(15, 23, 42, 0.14)",
                  border: "1px solid",
                  borderColor: "divider",
                  minWidth: 300,
                  overflow: "hidden",
                  bgcolor: "background.paper",
                  py: 0.8,
                },
              },
            }}
          >
            <Box
              sx={{
                px: 2,
                py: 1.4,
                display: "flex",
                alignItems: "center",
                gap: 1.2,
                bgcolor: "linear-gradient(135deg, rgba(25,118,210,0.08), rgba(25,118,210,0.02))",
              }}
            >
              <Avatar
                sx={{
                  width: 36,
                  height: 36,
                  bgcolor: "primary.main",
                  fontWeight: 700,
                }}
              >
                H
              </Avatar>
              <Box>
                <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                  Haris
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  haris@email.com
                </Typography>
              </Box>
            </Box>

            <Box sx={{ borderTop: "1px solid", borderColor: "divider", my: 0.8, mx: 1 }} />

            <MenuItem
              sx={{
                py: 1.3,
                px: 1.8,
                borderRadius: 1.5,
                mx: 1,
                mt: 0.5,
                fontWeight: 600,
                color: "text.primary",
                "&:hover": { bgcolor: "primary.50", color: "primary.main" },
              }}
            >
              Profile
            </MenuItem>

            <MenuItem
              sx={{
                py: 1.3,
                px: 1.8,
                borderRadius: 1.5,
                mx: 1,
                fontWeight: 600,
                color: "text.primary",
                "&:hover": { bgcolor: "primary.50", color: "primary.main" },
              }}
            >
              Settings
            </MenuItem>

            <Box sx={{ borderTop: "1px solid", borderColor: "divider", my: 0.8, mx: 1 }} />

            <MenuItem
              onClick={handleLogout}
              sx={{
                py: 1.3,
                px: 1.8,
                borderRadius: 1.5,
                mx: 1,
                mb: 0.5,
                fontWeight: 600,
                color: "error.main",
                "&:hover": { bgcolor: "error.50", color: "error.dark" },
              }}
            >
              Logout
            </MenuItem>
          </Menu>
        </Box>
      </Toolbar>
    </AppBar>
  );
}
