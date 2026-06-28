// 
import { Box, Paper, Typography } from "@mui/material";

interface StatCardProps {
    title: string;
    value: number;
    icon: React.ReactNode;
}

export default function StatCard({
    title,
    value,
    icon,
}: StatCardProps) {
    return (
        <Paper
            elevation={0}
            sx={{
                p: 3,
                borderRadius: 3,
                border: "1px solid",
                borderColor: "divider",
                height: "100%",
            }} 
        >
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    mb: 3,
                }}
            >
                <Box
                    sx={{
                        width: 42,
                        height: 42,
                        borderRadius: "50%",
                        bgcolor: "primary.light",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                    }}
                >
                    {icon}
                </Box>
                <Typography 
                    variant="body2"
                    color="text.secondary" 
                >
                    {title}
                </Typography>
                <Typography
                    variant="h3"
                    sx={{ fontWeight: 700 }}
                >
                    {value}
                </Typography>
            </Box>
        </Paper>
    );
}