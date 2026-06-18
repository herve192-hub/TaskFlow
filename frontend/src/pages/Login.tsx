import {
    Paper,
    Button,
    Typography,
    Box,
    TextField,
} from "@mui/material";

import { Link } from "react-router-dom";
import AuthLayout from "../components/AuthLayout";


export default function Login() {
    return (
        <AuthLayout>
            <Paper
                elevation={0}
                sx={{
                    width: "100%",
                    maxWidth: 450,
                    p: 5,
                    borderRadius: 4,
                    border:  "1px solid #E5E8F0",
                }}
            >
                <Typography variant="h4" 
                        sx={{ mb: 1, fontWeight: "bold" }}
                >
                    Welcom Back
                </Typography>
                <Typography 
                    color="text.secondary"
                    sx={{ mb: 4 }}
                >
                    Sign in to continue to TaskFlow
                </Typography>

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
                    // size="Large"
                    sx={{ size: "Large", mt: 3, py: 1.5,}}
                >
                    Sign In
                </Button>

                <Box sx={{textAlign: "center", mt: 2 }}>
                    <Typography>
                        Don't have an account?{" "}
                        <Link to="/signup">Sign up</Link>
                    </Typography>
                </Box>
            </Paper>
        </AuthLayout>
    );
}