import {
    Box,
    Drawer,
    Toolbar,
    AppBar,
    Typography,
} from "@mui/material";

import { ReactNode } from "react";
import Button from "@mui/material/Button";
import { useNavigate } from "react-router-dom";

const drawerWidth = 240;

interface Props {
    children: ReactNode;
}


export default function DashboardLayout({
    children,
}: Props) {
    const navigate = useNavigate();
    const handleLogout = () => {
        localStorage.clear();
        navigate("/");
    }
    return (
        <Box sx={{ display: "flex" }}>
            <AppBar
                position="fixed"
                sx={{
                    zIndex: 1201,
                }}
            >
                <Toolbar>
                    <Typography
                        sx={{variant: "h6",
                        fontWeight: "bold",
                        }}
                    >
                        TaskFlow
                    </Typography>
                    {/* <Button
                        color="inherit"
                        onClick={handleLogout}
                    >
                        Logout
                    </Button> */}
                </Toolbar>
            </AppBar>
            <Drawer
                variant="permanent"
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,

                    "& .MuiDrawer-paper": {
                        width: drawerWidth,
                        boxSizing: "border-box",
                    },
                }}
            >
                <Toolbar/>
                <Box sx={{p: "2" }}>
                    <Typography>
                        Dashboard
                    </Typography>
                    <Typography sx={{mt: "2"}}>
                        Projects
                    </Typography>
                    <Typography sx={{mt: "2"}}>
                        Tasks
                    </Typography>
                    <Typography sx={{mt: "2"}}>
                        Teams
                    </Typography>
                    <Typography sx={{mt: "2"}}>
                        Settings
                    </Typography>
                </Box>
                                <Button
                    color="inherit"
                    onClick={handleLogout}
                >
                    Logout
                </Button>
            </Drawer>
            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    p: 3,
                }}
            >
                <Toolbar/>
                {children}
            </Box>
        </Box>
    );
}