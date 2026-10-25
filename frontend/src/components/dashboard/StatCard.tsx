/**
 * Presentational dashboard metric card. The parent supplies a computed count, icon,
 * and theme tone; this component owns responsive layout and readable labels only.
 * Data loading and completeness decisions stay in Dashboard/useWorkspace.
 */

// 
import { Box, Paper, Typography } from "@mui/material";

interface StatCardProps {
    title: string;
    value: number;
    icon: React.ReactNode;
    tone?: "primary" | "secondary" | "info";
}

/** Display a parent-computed metric and icon using the shared dashboard card styling. */
export default function StatCard({
    title,
    value,
    icon,
    tone = "primary",
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
                overflow: "hidden",
                minWidth: 0,
            }} 
        >
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    flexWrap: { xs: "wrap", sm: "nowrap" },
                    gap: 2,
                    mb: 3,
                    minWidth: 0,
                }}
            >
                <Box
                    sx={{
                        width: 42,
                        height: 42,
                        borderRadius: "50%",
                        bgcolor: `${tone}.light`,
                        color: `${tone}.main`,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        flexShrink: 0,
                    }}
                >
                    {icon}
                </Box>
                <Typography 
                    variant="body2"
                    color="text.secondary"
                    sx={{
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                        whiteSpace: "nowrap",
                        minWidth: 0,
                        flexGrow: 1,
                    }}
                >
                    {title}
                </Typography>
                <Typography
                    variant="h3"
                    sx={{ fontWeight: 700, flexShrink: 0 }}
                >
                    {value}
                </Typography>
            </Box>
        </Paper>
    );
}
