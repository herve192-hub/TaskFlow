/**
 * Sign-in form using authService's HttpOnly-cookie and CSRF flow. Trims email but
 * preserves password bytes; validation and disabled controls improve feedback.
 * After login/profile restoration, navigate to the allowlisted saved start page.
 * Production builds may hide signup links; the server independently enforces onboarding.
 */

import { Box, Button, Link as MuiLink, TextField, Typography } from "@mui/material";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { toast } from "react-toastify";
import AuthLayout from "../components/AuthLayout";
import PasswordField from "../components/PasswordField";
import { authService } from "../features/auth/services/authService";
import { startPagePath } from "../features/users/utils/preferences";
import type { LoginRequest } from "../features/auth/types/auth.types";
import { apiErrorMessage } from "../utils/apiError";

/** Sign in with the server's cookie/CSRF contract, then restore the saved start page. */
export default function Login() {
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginRequest>();
  const signIn = async (data: LoginRequest) => {
    try {
      await authService.login({ email: data.email.trim(), password: data.password });
      toast.success("Login successful");
      navigate(startPagePath(authService.getCurrentUser()?.preferences));
    } catch (error) {
      toast.error(apiErrorMessage(error, "Unable to sign in. Check your email and password."));
    }
  };

  return <AuthLayout title="Welcome Back">
    <Box component="form" onSubmit={handleSubmit(signIn)}>
      <TextField fullWidth label="Email" type="email" autoComplete="username" margin="normal"
        disabled={isSubmitting} error={!!errors.email} helperText={errors.email?.message}
        slotProps={{ htmlInput: { maxLength: 254 } }}
        {...register("email", { required: "Email is required",
          pattern: { value: /^\S+@\S+$/i, message: "Invalid email address" } })} />
      <PasswordField label="Password" autoComplete="current-password" disabled={isSubmitting}
        error={!!errors.password} helperText={errors.password?.message}
        registration={register("password", { required: "Password is required", maxLength: 128 })} />
      <Box sx={{ mt: 1, textAlign: "right" }}>
        <MuiLink component={Link} to="/forgot-password" variant="body2">Forgot password?</MuiLink>
      </Box>
      <Button type="submit" fullWidth variant="contained" size="large" disabled={isSubmitting} sx={{ mt: 3 }}>
        {isSubmitting ? "Signing in..." : "Sign In"}
      </Button>
    </Box>
    <Typography color="text.secondary" sx={{ mt: 3, textAlign: "center" }}>
      {import.meta.env.VITE_PUBLIC_REGISTRATION_ENABLED === "false"
        ? "Contact your administrator for access."
        : <>Don't have an account? <Link to="/signup">Create one</Link></>}
    </Typography>
  </AuthLayout>;
}
