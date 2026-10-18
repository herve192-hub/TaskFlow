import { Box, Button, TextField, Typography } from "@mui/material";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { toast } from "react-toastify";
import AuthLayout from "../components/AuthLayout";
import PasswordField from "../components/PasswordField";
import { authService } from "../features/auth/services/authService";
import { apiErrorMessage } from "../utils/apiError";

interface SignupFormData {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
}

/** Keep form validation aligned with the auth API; account roles remain server-controlled. */
export default function Signup() {
  const navigate = useNavigate();
  const { register, handleSubmit, getValues, formState: { errors, isSubmitting } } = useForm<SignupFormData>();
  const createAccount = async (data: SignupFormData) => {
    try {
      await authService.register({
        firstname: data.firstName.trim(), lastname: data.lastName.trim(),
        username: data.username.trim(), email: data.email.trim().toLowerCase(), password: data.password,
      });
      toast.success("Account created successfully");
      navigate("/login");
    } catch (error) {
      toast.error(apiErrorMessage(error, "Unable to create account. Please try again."));
    }
  };

  return <AuthLayout title="Create Account" description="Start organizing your work today">
    <Box component="form" onSubmit={handleSubmit(createAccount)}>
      <TextField fullWidth label="First Name" autoComplete="given-name" margin="normal" disabled={isSubmitting}
        error={!!errors.firstName} helperText={errors.firstName?.message} slotProps={{ htmlInput: { maxLength: 100 } }}
        {...register("firstName", { validate: value => !!value.trim() || "First name is required" })} />
      <TextField fullWidth label="Last Name" autoComplete="family-name" margin="normal" disabled={isSubmitting}
        error={!!errors.lastName} helperText={errors.lastName?.message} slotProps={{ htmlInput: { maxLength: 100 } }}
        {...register("lastName", { validate: value => !!value.trim() || "Last name is required" })} />
      <TextField fullWidth label="Username" autoComplete="username" margin="normal" disabled={isSubmitting}
        error={!!errors.username} helperText={errors.username?.message} slotProps={{ htmlInput: { maxLength: 20 } }}
        {...register("username", { required: "Username is required",
          minLength: { value: 4, message: "Username must be at least 4 characters" } })} />
      <TextField fullWidth label="Email" type="email" autoComplete="email" margin="normal" disabled={isSubmitting}
        error={!!errors.email} helperText={errors.email?.message} slotProps={{ htmlInput: { maxLength: 254 } }}
        {...register("email", { required: "Email is required",
          pattern: { value: /^\S+@\S+$/i, message: "Invalid email address" } })} />
      <PasswordField label="Password" autoComplete="new-password" disabled={isSubmitting}
        error={!!errors.password} helperText={errors.password?.message}
        registration={register("password", { required: "Password is required",
          minLength: { value: 15, message: "Password must be at least 15 characters" }, maxLength: 128 })} />
      <PasswordField label="Confirm Password" autoComplete="new-password" disabled={isSubmitting}
        error={!!errors.confirmPassword} helperText={errors.confirmPassword?.message}
        registration={register("confirmPassword", { required: "Please confirm your password",
          validate: value => value === getValues("password") || "Passwords do not match" })} />
      <Button type="submit" fullWidth variant="contained" size="large" disabled={isSubmitting} sx={{ mt: 3 }}>
        {isSubmitting ? "Creating account..." : "Create Account"}
      </Button>
    </Box>
    <Typography color="text.secondary" sx={{ mt: 3, textAlign: "center" }}>
      Already have an account? <Link to="/login">Sign In</Link>
    </Typography>
  </AuthLayout>;
}
