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


interface SignupFormData {
    fullName: string;
    email: string;
    password: string;
    confirmPassword: string;
}

export default function Signup() {
    const { 
        register, 
        handleSubmit,
        watch,
        formState: { errors, isSubmitting },
     } = useForm<SignupFormData>();

     const password = watch("password");
     const onSubmit = async (data: SignupFormData) => {
        console.log(data);
        // 
        // Handle signup logic ...
     };
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
                      <TextField
                        fullWidth
                        label="Confirm Password"
                        type="password"
                        margin="normal"
                        error={!!errors.confirmPassword}
                        helperText={errors.confirmPassword?.message}
                        {...register("confirmPassword", { 
                            required: "Please confirm your password",
                            validate: (value) => 
                                value === password || "Passwords do not match",
                        })}
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