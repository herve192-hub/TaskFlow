import { AppBar, Avatar, Box, IconButton, Menu, MenuItem, Toolbar, Typography } from "@mui/material";
import { useState } from "react";
import { toast } from "react-toastify";
import { Link, useNavigate } from "react-router-dom";
import { collapsedDrawerWidth, drawerWidth } from "./layoutConstants";
import { authService } from "../../features/auth/services/authService";
import { useCurrentUser } from "../../features/auth/hooks/useCurrentUser";
import { userDisplayName } from "../../features/users/utils/userName";
import TaskFlowLogo from "../TaskFlowLogo";
import { brandGradient, brandWash } from "../../theme/theme";

const avatarStyle = {
  bgcolor: "primary.main", backgroundImage: brandGradient, color: "primary.contrastText",
};

/** Responsive branding and account actions backed by the active session. */
export default function Topbar({ collapsed }: { collapsed: boolean }) {
  const navigate = useNavigate();
  const user = useCurrentUser();
  const displayName = userDisplayName(user, user?.username?.trim() || "Your account");
  const parts = displayName.split(/\s+/);
  const initials = user ? [parts[0], ...(parts.length > 1 ? parts.slice(-1) : [])]
    .map(part => Array.from(part)[0]).join("").toLocaleUpperCase() : "?";
  const [anchor, setAnchor] = useState<HTMLElement | null>(null);
  const close = () => setAnchor(null);
  const logout = async () => {
    close();
    try {
      await authService.logout();
      navigate("/login", { replace: true });
    } catch {
      toast.error("Sign-out could not reach the server. Please retry.");
    }
  };

  return <AppBar position="fixed" sx={{
    zIndex: theme => theme.zIndex.drawer + 1,
    ml: { xs: 0, sm: (collapsed ? collapsedDrawerWidth : drawerWidth) + "px" },
    px: { xs: 1, sm: 2 }, bgcolor: "background.paper", color: "text.primary",
    boxShadow: "0px 2px 10px rgba(15, 23, 42, 0.08)", borderBottom: "1px solid", borderColor: "divider",
  }}>
    <Toolbar sx={{ justifyContent: "space-between", minHeight: { xs: 64, sm: 72 } }}>
      <Box component={Link} to="/dashboard" aria-label="TaskFlow dashboard" sx={{ flexShrink: 0 }}>
        <Box sx={{ display: { xs: "block", sm: "none" } }}><TaskFlowLogo variant="mark" /></Box>
        <Box sx={{ display: { xs: "none", sm: "block" }, width: { sm: 150, md: 180 } }}><TaskFlowLogo /></Box>
      </Box>
      <IconButton id="account-menu-button" aria-label={"Open account menu for " + displayName}
        aria-controls={anchor ? "account-menu" : undefined} aria-haspopup="menu"
        aria-expanded={!!anchor} onClick={event => setAnchor(event.currentTarget)} sx={{ p: 0.25 }}>
        <Avatar src={user?.profileImage || undefined} alt={displayName}
          sx={{ ...avatarStyle, width: { xs: 34, sm: 40 }, height: { xs: 34, sm: 40 },
            border: "2px solid", borderColor: "primary.light" }}>{initials}</Avatar>
      </IconButton>
      <Menu id="account-menu" anchorEl={anchor} open={!!anchor} onClose={close}
        slotProps={{ list: { "aria-labelledby": "account-menu-button" },
          paper: { sx: { mt: 1.6, minWidth: 280, maxWidth: 360, borderRadius: 2,
            border: "1px solid", borderColor: "divider" } } }}>
        <Box sx={{ px: 2, py: 1.5, display: "flex", alignItems: "center", gap: 1.5, background: brandWash }}>
          <Avatar src={user?.profileImage || undefined} alt={displayName}
            sx={{ ...avatarStyle, width: 36, height: 36 }}>{initials}</Avatar>
          <Box sx={{ minWidth: 0 }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 700, overflowWrap: "anywhere" }}>{displayName}</Typography>
            {user?.email && <Typography variant="caption" color="text.secondary" sx={{ overflowWrap: "anywhere" }}>{user.email}</Typography>}
          </Box>
        </Box>
        <MenuItem component={Link} to="/dashboard/profile" onClick={close}>Profile</MenuItem>
        <MenuItem component={Link} to="/dashboard/settings" onClick={close}>Settings</MenuItem>
        <MenuItem onClick={() => { void logout(); }} sx={{ color: "error.main" }}>Logout</MenuItem>
      </Menu>
    </Toolbar>
  </AppBar>;
}
