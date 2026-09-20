import {
    Box,
    Button,
    TextField,
    Typography,
    Paper,
} from "@mui/material";

import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import AuthLayout from "../components/AuthLayout.tsx";
import { toast } from "react-toastify";

import { useState } from "react";
import InputAdornment from "@mui/material/InputAdornment";
import IconButton from "@mui/material/IconButton";
import Visibility from "@mui/icons-material/Visibility";
import VisibilityOff from "@mui/icons-material/VisibilityOff";
import axios from "axios";

import { authService } from "../features/auth/services/authService";
import type { RegisterRequest } from "../features/auth/types/auth.types";

interface SignupFormData {
    firstName: string;
    lastName: string;
    username: string;
    email: string;
    password: string;
    confirmPassword: string;
}

export default function Signup() {
    const navigate = useNavigate();

    const {
        register,
        handleSubmit,
        getValues,
        formState: { errors },
    } = useForm<SignupFormData>();

    const onSubmit = async (data: SignupFormData) => {
        const request: RegisterRequest = {
            firstname: data.firstName.trim(),
            lastname: data.lastName.trim(),
            username: data.username.trim(),
            email: data.email.trim().toLowerCase(),
            password: data.password,
        };

        try {
            await authService.register(request);
            toast.success("Account created successfully");
            navigate("/login");
        } catch (error) {
            if (axios.isAxiosError(error)) {
                console.error("Registration failed:", {
                    status: error.response?.status,
                    data: error.response?.data,
                    message: error.message,
                });

                const message =
                    error.response?.data?.message ||
                    error.response?.data?.error ||
                    "Unable to create account";

                toast.error(message);
                return;
            }

            console.error("Unexpected registration error:", error);
            toast.error("Something went wrong");
        }
    };
// Handle password visibility toggles ...
     const [showPassword, setShowPassword] = useState(false);
// handle confirm password visibility toggle ...
     const [showConfirmPassword, setShowConfirmPassword] = useState(false);
     
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
                <form onSubmit={handleSubmit(onSubmit)}>
                    <TextField
                        fullWidth
                        label="First Name"
                        margin="normal"
                        error={!!errors.firstName}
                        helperText={errors.firstName?.message}
                        {...register("firstName", { 
                            required: "First name is required",
                         })}
                    />
                    <TextField
                        fullWidth
                        label="Last Name"
                        margin="normal"
                        error={!!errors.lastName}
                        helperText={errors.lastName?.message}
                        {...register("lastName", { 
                            required: "Last name is required",
                         })}
                    />
                    <TextField
                        fullWidth
                        label="Username"
                        margin="normal"
                        error={!!errors.username}
                        helperText={errors.username?.message}
                        {...register("username", { 
                            required: "Username is required",
                         })}
                    />
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
                        type={showPassword ? "text" : "password"}
                        margin="normal"
                        error={!!errors.password}
                        helperText={errors.password?.message}
                        {...register("password", {
                            required: "Password is required",
                            minLength: {
                                value: 8,
                                message: "Password must be at least 8 characters",
                            },
                            pattern: {
                                value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/,
                                message: "Password must contain uppercase, lowercase and a number",
                            },
                        })}
                    // Add visibility toggle for password field ...
                        slotProps={{
                            input: {
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton
                                            onMouseDown={(e) => e.preventDefault()}
                                            onClick={() => setShowPassword((prev) => !prev)}
                                            edge="end"
                                        >
                                            {showPassword ? (<VisibilityOff />) : (<Visibility />)}
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            },
                        }}
                    />
                    <TextField
                        fullWidth
                        label="Confirm Password"
                        type={showConfirmPassword ? "text" : "password"}
                        margin="normal"
                        error={!!errors.confirmPassword}
                        helperText={errors.confirmPassword?.message}
                        {...register("confirmPassword", { 
                            required: "Please confirm your password",
                            validate: (value) => 
                                value === getValues("password") || "Passwords do not match",
                        })}
                    // Add visibility toggle for confirm password field ...
                        slotProps={{
                            input: {
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton
                                            onMouseDown={(e) => e.preventDefault()}
                                            onClick={() => setShowConfirmPassword((prev) => !prev)}
                                            edge="end"
                                        >
                                            {showConfirmPassword ? (<VisibilityOff />) : (<Visibility />)}
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            },
                        }}
                    />
                    <Button
                    type="submit"
                        fullWidth
                        variant="contained"
                        sx={{ 
                            size: "large",
                            mt: 3, 
                            py: 1.5,
                        }}
                    >
                        Create Account
                    </Button>
                </form>
                <Box 
                    sx={{ 
                        textAlign: "center",  
                        mt: 3, 
                        color: "text.secondary",
                    }}
                >
                     <Typography>
                        Already have an account?{" "}
                        <Link to="/login"> Sign In </Link>
                     </Typography>
                </Box>
            </Paper>
        </AuthLayout>
    );
}
