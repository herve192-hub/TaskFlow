// 
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
import { toast } from "react-toastify";

import { useState } from "react";
import InputAdornment from "@mui/material/InputAdornment";
import IconButton from "@mui/material/IconButton";
import Visibility from "@mui/icons-material/Visibility";
import VisibilityOff from "@mui/icons-material/VisibilityOff";

import Divider from "@mui/material/Divider";
import GoogleIcon from "@mui/icons-material/Google";
import GitHubIcon from "@mui/icons-material/GitHub";
import OAuthButton from "../components/oAuthButtonProps.tsx";

import Stack from "@mui/material/Stack";
import Checkbox from "@mui/material/Checkbox";
import FormControlLabel from "@mui/material/FormControlLabel";
import MuiLink from "@mui/material/Link";



interface LoginFormData {
    email: string;
    password: string;
    rememberMe: boolean;
}

export default function Login() {
// Initialize form handling with react-hook-form ...
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<LoginFormData>({
        defaultValues: {
            rememberMe: false,
        },
    });
// Handle form submission ...
    const onSubmit = async (data: LoginFormData) => {
        console.log(data);

        toast.success(
            "Login successful!"
        );
        // 
        // handle login logic ...
    };
// handle password visibility toggle ...
    const [showPassword, setShowPassword] = useState(false);
// 
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
                    sx={{ 
                        color: "text.secondary",
                        mb: 4,
                     }}
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
                    <Box 
                        sx={{ 
                            display: "flex", 
                            justifyContent: "space-between", 
                            alignItems: "center", 
                            mt: 1, 
                            mb: 2,

                        }}
                    >
                        <FormControlLabel
                            control={
                                <Checkbox
                                    {...register("rememberMe")}
                                    color="primary"
                                /> 
                            }
                            label="Remember me"
                        />
                        <MuiLink
                            variant="body2"
                            component={Link}
                            to="/forgot-password"
                            // href="/forgot-password"
                            // color="primary"
                            sx={{ 
                                textDecoration: "none",
                                cursor: "pointer",
                                fontWeight: "500",
                             }}
                        >
                            Forgot password?
                        </MuiLink>
                    </Box>
                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        // size="Large"
                        sx={{ size: "Large", mt: 3, py: 1.5 }}
                    >
                        Sign In
                    </Button>
                    <Stack 
                        direction="column" 
                        spacing={2}
                    >
                        <Divider sx={{ my: 4 }}> or </Divider>
                        <OAuthButton 
                            text="Continue with Google"
                            icon={<GoogleIcon />}
                        />
                        <OAuthButton 
                            text="Continue with GitHub"
                            icon={<GitHubIcon />}
                            // sx={{ mt: 2 }}
                        />
                    </Stack>
                </form>
                <Box 
                    sx={{
                        textAlign: "center", 
                        mt: 3,
                        color: "text.secondary",
                    }}
                >
                    <Typography>
                        Don't have an account?{" "}
                        <Link to="/signup">Create one</Link>
                    </Typography>
                </Box>
            </Paper>
        </AuthLayout>
    );
}