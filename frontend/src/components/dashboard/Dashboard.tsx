// Dashboard.tsx ...

import {
    Box,
    Typography,
    Grid,
} from "@mui/material";
import StatCard from "./StatCard";

import TaskAltIcon from "@mui/icons-material/TaskAlt";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import FolderOpenIcon from "@mui/icons-material/FolderOpen";
import QuickActions from "./QuickActions";

export default function Dashboard() {
    return (
        <Box 
            sx={{ p: 4 }}
        >
            <Typography 
                variant = "h4"
                sx={{
                    fontWeight: "bold", mb: 1, }}
            >
                Welcome Back
            </Typography>
            <Typography
                color="text.secondary"
                sx={{ mb: 4 }}
            >
                Here's what's happening today.
            </Typography>
            <Grid container spacing={3}>
                <Grid size={{xs: 12, md: 4}}>
                    <StatCard
                        title="Total Tasks"
                        value={42}
                        icon={<TaskAltIcon color="primary" />}
                    />
                </Grid>
                <Grid size={{xs: 12, md: 4}}>
                    <StatCard
                        title="Completed"
                        value={28}
                        icon={<CheckCircleIcon color="primary" />}
                    />
                </Grid>
                <Grid size={{xs: 12, md: 4}}>
                    <StatCard
                        title="Projects"
                        value={6}
                        icon={<FolderOpenIcon color="primary" />}
                    />
                </Grid>
            </Grid>
            <QuickActions/>
        </Box>
    );
}