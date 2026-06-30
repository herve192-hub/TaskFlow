import {
    Paper,
    Typography,
    LinearProgress,
    Stack,
} from "@mui/material"

const items = [
    {title: "Projects", value: 82 },
    {title: "Task", value: 67 },
    {title: "Team", value: 91 },
];

export default function ProgressWidget() {
    return (
        <Paper
            elevation={0}
            sx={{
                p: 3,
                borderRadius: 3,
                border: "1px solid",
                borderColor: "divider",
                height: "100%",
                display: "flex",
                flexDirection: "column",
            }}
        >
            <Typography
                variant="h6"
                sx={{
                    fontWeight: 700,
                    mb: 3,
                }}
            >
                Progress
            </Typography>
            <Stack spacing={3}>
                {
                    items.map((item) => (
                        <div key={item.title}>
                            <Typography sx={{ mb: 1 }}>
                                {item.title}
                            </Typography>
                            <LinearProgress
                                variant="determinate"
                                value={item.value}
                                sx={{
                                    height: 10,
                                    borderRadius: 10,
                                }}
                            />
                            <Typography 
                                variant="body2"
                                sx={{
                                    mt: 1,
                                    color: "text.secondary"
                                }}
                            >
                                {item.value}%
                            </Typography>
                        </div>
                    ))
                }
            </Stack>
        </Paper>
    );
}