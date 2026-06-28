// 
import {
    Paper,
    Typography,
    Box,
    Avatar,
    Divider,
    Chip,
} from "@mui/material";

import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import FolderOpenIcon from "@mui/icons-material/FolderOpen";
import GroupAddIcon from "@mui/icons-material/GroupAdd";
import PersonIcon from "@mui/icons-material/Person";


const activities = [
    {
        title: 'Completed "Design Login Page"',
        time: " 5 min ago",
        type: "Completed",
        icon: <CheckCircleIcon fontSize="small" />,
        color: "success",
    },
    {
        title: 'Created Project "TaskFlow"',
        time: "1 hour ago",
        type: "Project",
        icon: <FolderOpenIcon fontSize="small" />,
        color: "primary",
    },
    {
        title: "Invited Herve Chendjou",
        time: "Yesteday",
        type: "Team",
        icon: <GroupAddIcon fontSize="small" />,
        color: "warning",
    },
    {
        title: "Updated Profile",
        time: "2 days ago",
        type: "Profile",
        icon: <PersonIcon fontSize="small" />,
        color: "secondary",
    },
];

export default function RecentAtivity() {
    return (
        <Paper
            elevation={0}
            sx={{
                mt: 4,
                p: 3,
                borderRadius: 3,
                border: "1px solid",
                borderColor: "divider",
            }}
        >
            <Typography
                variant="h6"
                sx={{
                    fontWeight: 700,
                    mb: 3, 
                }}
            >
                Recent Activity
            </Typography>
            {activities.map((activity, index) => (
                <Box key={index}>
                    <Box
                        sx={{
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "space-between",
                            py: 2,
                        }}
                    >
                        <Box
                            sx={{
                                display: "flex",
                                alignContent: "center",
                                gap: 2,
                            }}
                        >
                            <Avatar
                                sx={{
                                    bgcolor: `${activity.color}.light`,
                                    color: `${activity.color}.main`,
                                    width: 42,
                                    height: 42,
                                }}
                            >
                                {activity.icon}
                            </Avatar>
                            <Box>
                                <Typography
                                    sx={{ fontWeight: 600 }}
                                >
                                    {activity.title}
                                </Typography>
                                <Typography
                                    variant="body2"
                                    color="text.secondary"
                                >
                                    {activity.time}
                                </Typography>
                            </Box>
                        </Box>
                        <Chip
                            label={activity.type}
                            color={
                                activity.color as
                                    | "primary"
                                    | "secondary"
                                    | "success"
                                    | "warning"
                            }
                            size="small" />
                    </Box>
                    {index !== activities.length - 1 && (
                        <Divider />
                    )}
                </Box>
            ))}
        </Paper>
    );
}