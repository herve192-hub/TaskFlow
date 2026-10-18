import { Box, Paper, Typography } from "@mui/material";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import TaskFlowLogo from "./TaskFlowLogo";
import { brandWash } from "../theme/theme";

interface Props {
  title: string;
  description?: string;
  children: ReactNode;
}

/** Shared branding, card layout, and headings for the authentication pages. */
export default function AuthLayout({ title, description, children }: Props) {
  return <Box sx={{ minHeight: "100vh", display: "flex", bgcolor: "background.default" }}>
    <Box sx={{ flex: 1, background: brandWash, display: { xs: "none", md: "flex" },
      flexDirection: "column", justifyContent: "center", px: 8 }}>
      <Box component={Link} to="/" aria-label="TaskFlow home" sx={{ width: "fit-content", mb: 3 }}>
        <TaskFlowLogo variant="stacked" />
      </Box>
      <Typography variant="h6" sx={{ mb: 5 }}>
        Manage projects, track progress, and collaborate efficiently from anywhere.
      </Typography>
      <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
        {["Project management", "Task workflows", "Team collaboration", "Profiles and preferences"].map(feature =>
          <Typography key={feature}>✓ {feature}</Typography>)}
      </Box>
    </Box>
    <Box sx={{ flex: 1, display: "flex", flexDirection: "column", justifyContent: "center",
      alignItems: "center", p: { xs: 2, sm: 3 } }}>
      <Box component={Link} to="/" aria-label="TaskFlow home"
        sx={{ display: { xs: "block", md: "none" }, mb: 3 }}>
        <TaskFlowLogo width={220} />
      </Box>
      <Paper elevation={0} sx={{ width: "100%", maxWidth: 480, p: { xs: 3, sm: 5 },
        borderRadius: 4, border: "1px solid", borderColor: "divider" }}>
        <Typography variant="h4" sx={{ mb: description ? 1 : 3, fontWeight: 700 }}>{title}</Typography>
        {description && <Typography color="text.secondary" sx={{ mb: 3 }}>{description}</Typography>}
        {children}
      </Paper>
    </Box>
  </Box>;
}
