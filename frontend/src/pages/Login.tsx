import {
    Paper,
    Button,
    Typography,
    Box,
    TextField,
} from "@mui/material";

import { Link } from "react-router-dom";
import { useForm } from "react-hook-form";
import AuthLayout from "../components/AuthLayout";

interface LoginFormData {
    email: string;
    password: string;
}

export default function Login() {

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<LoginFormData>();

    const onSubmit = async (data: LoginFormData) => {
        console.log(data);
        // 
        // handle login logic ...
    };
    return (
        <AuthLayout>
            <Paper
                elevation={0}
                sx={{
                    width: "100%",
                    maxWidth: 450,
                    p: 5,
                    borderRadius: 4,
                    border:  "1px solid #E2E8F0",
                }}
            >
                <Typography variant="h4" 
                        sx={{ mb: 1, fontWeight: "bold" }}
                >
                    Welcome Back
                </Typography>
                <Typography 
                    color="text.secondary"
                    sx={{ mb: 4 }}
                >
                    Sign in to continue to TaskFlow
                </Typography>

                <form onSubmit={handleSubmit(onSubmit)}>
                    <TextField
                        fullWidth
                        label="Email"
                        margin="normal"
                        error={!!errors.email}
                        helperText={errors.email?.message}
                        {...register("email", {
                            required: "Email is required",
                            pattern: {
                                value: /^\S+@\S+$/i,
                                message: "Invalid email address",
                            },
                        })}
                    />
                    <TextField
                        fullWidth
                        label="Password"
                        type="password"
                        margin="normal"
                        error={!!errors.password}
                        helperText={errors.password?.message}
                        {...register("password", {
                            required: "Password is required",
                            minLength: {
                                value: 8,
                                message: "Password must be at least 8 characters",
                            },
                        })}
                    /> 
                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        // size="Large"
                        sx={{ size: "Large", mt: 3, py: 1.5 }}
                    >
                        Sign In
                    </Button>
                </form>

                <Box sx={{textAlign: "center", mt: 3 }}>
                    <Typography>
                        Don't have an account?{" "}
                        <Link to="/signup">Create one</Link>
                    </Typography>
                </Box>
            </Paper>
        </AuthLayout>
    );
}