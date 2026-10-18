import { Typography, Button } from "@mui/material";
import { Link } from "react-router-dom";
import AuthLayout from "../components/AuthLayout";

/** Recovery guidance until an authenticated account-recovery process is implemented. */
export default function ForgotPassword() {
  return <AuthLayout title="Account recovery">
    <Typography color="text.secondary" sx={{ mb: 3 }}>
      Password recovery is not configured. Contact your TaskFlow administrator to recover your account.
    </Typography>
    <Button component={Link} to="/login" variant="contained">Back to sign in</Button>
  </AuthLayout>;
}
