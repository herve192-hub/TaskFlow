import {
    Paper,
    Typography,
    List,
    ListItem,
    ListItemText,
    Chip,
} from "@mui/material";

type DeadlineColor = "error" | "warning" | "success";

const deadlines: Array<{
    project: string;
    due: string;
    color: DeadlineColor;
}> = [
    {
        project: "UI Redesign",
        due: "Tomorrow",
        color: "error",
    },
    {
        project: "Authentication",
        due: "Friday",
        color: "warning",
    },
    {
        project: "MongoDB Schema",
        due: "Next Week",
        color: "success",
    },
];


export default function UpcomingDeadlines() {
    return (
        <Paper
            elevation={0}
            sx={{
                borderRadius: 3,
                border: "1px solid",
                borderColor: "divider",
                p: 3,
                overflow: "hidden",
                height: "100%",
                display: "flex",
                flexDirection: "column",
            }}
        >
            <Typography
                variant="h6"
                sx={{
                    fontWeight: 700,
                    mb: 2,
                }}
            >
                Upcoming Deadlines
            </Typography>
            <List disablePadding sx={{ width: "100%" }}>
                {deadlines.map((item) => (
                    <ListItem
                        key={item.project}
                        disablePadding
                        sx={{
                            py: 1,
                            px: 0,
                            alignItems: "center",
                            overflow: "hidden",
                        }}
                    >
                        <ListItemText
                            primary={item.project}
                            secondary={item.due}
                            sx={{
                                minWidth: 0,
                                mr: 1.5,
                                "& .MuiListItemText-primary": {
                                    overflow: "hidden",
                                    textOverflow: "ellipsis",
                                    whiteSpace: "nowrap",
                                },
                                "& .MuiListItemText-secondary": {
                                    overflow: "hidden",
                                    textOverflow: "ellipsis",
                                    whiteSpace: "nowrap",
                                },
                            }}
                        />
                        <Chip
                            size="small"
                            label={item.due}
                            color={item.color}
                            sx={{ flexShrink: 0 }}
                        />
                    </ListItem>
                ))}
            </List>
        </Paper>
    );
}