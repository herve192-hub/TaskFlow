import { Paper, Typography, Button } from '@mui/material'
import { Link } from 'react-router-dom'
import AuthLayout from '../components/AuthLayout'

/** Recovery guidance only; no recovery request or email-delivery operation is implemented here. */
export default function ForgotPassword() {
  return <AuthLayout>
    <Paper elevation={0} sx={{ width: '100%', maxWidth: 450, p: 5, borderRadius: 4, border: '1px solid', borderColor: 'divider' }}>
      <Typography variant="h4" sx={{ mb: 2, fontWeight: 'bold' }}>Account recovery</Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        Password recovery is not configured. Contact your TaskFlow administrator to recover your account.
      </Typography>
      <Button component={Link} to="/login" variant="contained">Back to sign in</Button>
    </Paper>
  </AuthLayout>
}
