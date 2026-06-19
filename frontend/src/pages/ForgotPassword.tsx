import {
    Paper,
    Typography,
    TextField,
    Button,
} from "@mui/material";

import { useForm } from "react-hook-form";
import AuthLayout from "../components/AuthLayout.tsx";

interface ForgotPasswordFormData {
    email: string;
}

export default function ForgotPassword() {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<ForgotPasswordFormData>();

    const onSubmit = async (
        data: ForgotPasswordFormData
        ) => {
        console.log(data);
        // 
        // Handle forgot password logic ...
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
                    border: "1px solid #E2E8F0",
                }}
            >
                <Typography 
                    variant="h4" 
                    sx={{ 
                        mb: 1, 
                        fontWeight: "bold",
                    }}
                >
                    Forgot Password
                </Typography>
                <Typography 
                    sx={{ 
                        color: "text.secondary",
                        mb: 4,
                     }}
                >
                    Enter your email and we'll send you a link to reset your password.
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
                    <Button
                        type="submit"
                        variant="contained"
                        fullWidth
                        sx={{ 
                            mt: 2,
                            size: "Large", 
                            py: 1.5,
                         }}
                    >
                        Send Reset Link
                    </Button>
                </form>
            </Paper>
        </AuthLayout>
    );
}