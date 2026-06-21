import {
    Box,
    Typography,
    Paper,
    Grid,
} from "@mui/material";

import DashboardLayout from "../components/DashboardLayout";


export default function Dashboard() {
    return (
        <DashboardLayout>
            <Box 
                sx={{ p: 4,
                variant: "h4",
                }}>
                <Typography
                    sx={{
                    fontWeight: "bold",
                    mb: 3, 
                    }}
                >
                    Welcome Back
                </Typography>

                <Grid container spacing={3}>
                    <Grid size={{ xs: 12, md: 4 }}>
                        <Paper sx={{p: 3}}>
                            <Typography variant="h6">
                                Total Tasks
                            </Typography>

                            <Typography variant="h3">
                                42
                            </Typography>
                        </Paper>
                    </Grid>
                    <Grid size={{ xs: 12, md: 4 }}>
                        <Paper sx={{ p: 3 }}>
                            <Typography variant="h6">
                                Completed
                            </Typography>
                            <Typography variant="h3">
                                28
                            </Typography>
                        </Paper>
                    </Grid>
                    <Grid size={{ xs: 12, md: 4 }}>
                        <Paper sx={{p: 3}}>
                            <Typography variant="h6">
                                Projects
                            </Typography>
                            <Typography variant="h3">
                                6
                            </Typography>
                        </Paper>
                    </Grid>
                </Grid>
            </Box>
        </DashboardLayout>
    );
}