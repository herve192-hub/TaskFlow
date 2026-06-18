import {
    Box,
    Button,
    TextField,
    Typography,
    Paper,
} from "@mui/material";

import { Link} from "react-router-dom";
import AuthLayout from "../components/AuthLayout.tsx";


export default function Signup() {
    return (
        <AuthLayout>
            <Paper
                sx={{
                    elevation: 0,
                     width: "100%", 
                     maxwidth: 500, p: 5, 
                     borderRadius: 4, 
                     border: "1px solid #E2E8F0",
                    }} 
            >
                <Typography
                    sx={{ 
                        variant: "h4", 
                        fontWeight: "bold", 
                        mb: 1,
                    }}
                >
                    Create Account
                </Typography>
                <Typography
                    sx={{
                        color: "text.secondary", 
                        mb: 4,
                    }}
                >
                    Start organizing your work today
                </Typography>
                <TextField
                    fullWidth
                    label="Full Name"
                    margin="normal"
                />
                <TextField
                    fullWidth
                    label="Email"
                    margin="normal"
                />
                <TextField
                    fullWidth
                    label="Password"
                    type="password"
                    margin="normal"
                />

                <Button
                    fullWidth
                    variant="contained"
                    sx={{ 
                        mt: 3, 
                        mb: 1.5,
                     }}
                >
                    Create Account
                </Button>

                <Box 
                    sx={{
                        textAlign: "center", 
                        mt: 3,
                    }}
                >
                     <Typography>
                        Already have an account?{" "}
                        <Link to="/login">
                            Sign In
                        </Link>
                     </Typography>
                </Box>
            </Paper>
        </AuthLayout>
    );
}