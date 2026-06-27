import {
    Box,
    Typography,
    Paper,
    Grid,
} from "@mui/material";


export default function Dashboard() {
    return (
        <Box 
            sx={{ 
                mb: 4,
                p: 4,
            }}
        >
            <Typography
                sx={{
                    variant: "h4",
                    fontWeight: "bold",
                    mb: 3, 
                }}
            >
                Welcome Back
            </Typography>
            <Typography
                color="text.secondary"
                sx={{
                    color: "text.secondary",
                    pt: 4,
                    mb: 4
                }}
            >
                Here's what's happening today.
            </Typography>

            <Grid container spacing={4} sx={{pt: 4}}>
                <Grid size={{ xs: 12, md: 4 }} >
                    <Paper 
                        elevation={0}
                        sx={{
                            p: 3,
                            borderRadius: 3,
                            border: "1px solid #E2E8F0",
                        }}
                    >
                        <Typography 
                            variant="h6"
                            color="text.secondary"
                        >
                            Total Tasks
                        </Typography>

                        <Typography 
                            variant="h3"
                            color="text.secondary"
                        >
                            42
                        </Typography>
                    </Paper>
                </Grid>
                <Grid size={{ xs: 12, md: 4 }}>
                    <Paper 
                        elevation={0}
                        sx={{
                            p: 3,
                            borderRadius: 3,
                            border: "1px solid #E2E8F0",
                        }}>
                        <Typography 
                            variant="h6"
                            color="text.secondary"
                        >
                            Completed
                        </Typography>
                        <Typography 
                            variant="h3"
                            color="text.secondary"
                        >
                            28
                        </Typography>
                    </Paper>
                </Grid>
                <Grid size={{ xs: 12, md: 4 }}>
                    <Paper 
                        elevation={0}
                        sx={{
                            p: 3,
                            borderRadius: 3,
                            border: "1px solid #E2E8F0",
                        }}>
                        <Typography 
                            variant="h6" 
                            color="text.secondary"
                        >
                            Projects
                        </Typography>
                        <Typography 
                            variant="h3"
                            color="text.secondary"
                        >
                            6
                        </Typography>
                    </Paper>
                </Grid>
            </Grid>
        </Box>
    );
}