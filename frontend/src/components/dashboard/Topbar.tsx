// 
import { AppBar, Avatar, Box, IconButton, Menu, MenuItem, Toolbar, Typography } from "@mui/material";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import SearchIcon from "@mui/icons-material/Search";
import NotificationsIcon from "@mui/icons-material/Notifications";

import { drawerWidth } from "./layoutConstants";

export default function Topbar() {
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

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
        ml: `${drawerWidth}px`,
        px: { xs: 1, sm: 2 },
        backgroundColor: "background.paper",
        color: "text.primary",
        boxShadow: "0px 2px 10px rgba(15, 23, 42, 0.08)",
        borderBottom: "1px solid",
        borderColor: "divider",
      }}
    >
      <Toolbar sx={{ justifyContent: "space-between", minHeight: 72 }}>
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
          <Typography variant="h6" sx={{ fontWeight: 700, letterSpacing: 0.3 }}>
            TaskFlow
          </Typography>
        </Box>

        <Box sx={{ display: "flex", alignItems: "center", gap: 1.2 }}>
          <IconButton
            sx={{
              border: "1px solid",
              borderColor: "divider",
              bgcolor: "grey.50",
              "&:hover": { bgcolor: "grey.100" },
            }}
          >
            <SearchIcon />
          </IconButton>

          <IconButton
            sx={{
              border: "1px solid",
              borderColor: "divider",
              bgcolor: "grey.50",
              "&:hover": { bgcolor: "grey.100" },
            }}
          >
            <NotificationsIcon />
          </IconButton>

          <Avatar
            onClick={handleClick}
            sx={{
              bgcolor: "primary.main",
              cursor: "pointer",
              width: 40,
              height: 40,
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
