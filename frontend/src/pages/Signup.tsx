import {
    Box,
    Button,
    TextField,
    Typography,
    Paper,
} from "@mui/material";

import { Link} from "react-router-dom";
import { useForm } from "react-hook-form";
import AuthLayout from "../components/AuthLayout.tsx";

import { useState } from "react";
import InputAdornment from "@mui/material/InputAdornment";
import IconButton from "@mui/material/IconButton";
import Visibility from "@mui/icons-material/Visibility";
import VisibilityOff from "@mui/icons-material/VisibilityOff";



interface SignupFormData {
    fullName: string;
    email: string;
    password: string;
    confirmPassword: string;
}

export default function Signup() {
// Initialize form handling with react-hook-form ...
    const { 
        register, 
        handleSubmit,
        watch,
        formState: { errors, isSubmitting },
     } = useForm<SignupFormData>();
// Watch password field for confirm password validation ...
     const password = watch("password");
// Handle form submission ...
     const onSubmit = async (data: SignupFormData) => {
        console.log(data);
        // 
        // Handle signup logic ...
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
                        label="Full Name"
                        margin="normal"
                        error={!!errors.fullName}
                        helperText={errors.fullName?.message}
                        {...register("fullName", { 
                            required: "Full name is required",
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
                                value === password || "Passwords do not match",
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
                    sx={{ textAlign: "center",  mt: 3, }}
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