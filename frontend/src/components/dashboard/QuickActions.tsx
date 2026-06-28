import {
    Paper,
    Typography,
    Grid,
    Box,
    // Divider,
} from "@mui/material";

import AddTaskIcon from "@mui/icons-material/AddTask";
import CreateNewFolderIcon from "@mui/icons-material/CreateNewFolder";
import GroupAddIcon from "@mui/icons-material/GroupAdd";
import AssessmentIcon from "@mui/icons-material/Assessment";

import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";

const actions = [
    {
        title: "New Task",
        subtitle: "Create a new task",
        icon: <AddTaskIcon color="success" />,
        path: "/tasks",
    },
    {
        title: "New Project",
        subtitle: "Start a new project",
        icon: <CreateNewFolderIcon color="success" />,
        path: "/projects"
    },
    {
        title: "Invite Team",
        subtitle: "Add new members",
        icon: <GroupAddIcon color="warning" />,
        path: "/teams",
    },
    {
        title: "Reports",
        subtilte: "view analtics",
        icon: <AssessmentIcon color="secondary" />,
        path: "",
    },
];

export default function QuickActions() {
    const navigate = useNavigate();
    const handlClick = (path: string, title: string) => {
        if (path) {
            navigate(path);
        } else {
            toast.info(`${title} is coming soon.`);
        }
    };
    return (
        <Paper
            elevation={0}
            sx={{
                p: 3,
                mt: 4,
                borderRadius: 3,
                border: "1px solid",
                borderColor: "Divider",
            }}
        >
            <Typography
                variant="h6"
                sx={{
                    fontWeight: 700,
                    mb: 3, 
                }}
            >
                Quick Actions
            </Typography>
            <Grid container spacing={3}>
                {actions.map((action) => (
                    <Grid
                        key={action.title}
                        size={{
                            xs: 12,
                            sm: 6,
                            md: 3,
                        }}
                    >
                        <Paper
                            elevation={0}
                            onClick={() =>
                                handlClick(action.path, action.title)
                            }
                            sx={{
                                p: 3,
                                cursor: "pointer",
                                borderRadius: 3,
                                border: "1px solid",
                                borderColor: "divider",
                                height: "100%",
                                transition: "all.25s ease",

                                "&:hover": {
                                    transform: "translateY(-px)",
                                    boxShadow: 6,
                                    borderColor: "primary.main",
                                },

                                 "&:active": {
                                    transform: "translateY(-px)",
                                },
                            }}
                        >
                            <Box
                                sx={{
                                    width: 56,
                                    height: 56,
                                    borderRadius: 2,
                                    bgcolor: "grey.100",
                                    justifyContent: "center",
                                    alignItems: "center",
                                    mb: 2,
                                }}
                            >
                                {action.icon}
                            </Box>
                            <Typography
                                variant="h6"
                                sx={{fontWeight: 700 }}
                            >
                                {action.title}
                            </Typography>
                            <Typography
                                variant="body2"
                                sx={{ color: "text.secondary", mt: 1 }}
                            >
                                {action.subtitle}
                            </Typography>

                            <Box 
                                sx={{
                                    "& .action-arrow": {
                                        opacity: 0,
                                        transform: "translateX(-8px)",
                                        transition: "all .25s",
                                    },
                                    "&:hover .action-arrow": {
                                        opacity:1,
                                        tranform: "translateX(0)",
                                    },
                                }}
                            >
                                <Box
                                    className="action-arrow"
                                    sx={{
                                        display: "flex",
                                        justifyContent: "flex-end",
                                        mt: 2,
                                    }}
                                >
                                    <ArrowForwardIcon
                                        fontSize="small"
                                        // color="action"
                                    />
                                </Box>
                            </Box>
                        </Paper>
                    </Grid>
                ))}
            </Grid>
        </Paper>
    );
}