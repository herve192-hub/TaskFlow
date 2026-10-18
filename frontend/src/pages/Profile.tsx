import { useRef, useState } from "react";
import type { ChangeEvent } from "react";
import { useForm } from "react-hook-form";
import { Link as RouterLink } from "react-router-dom";
import { Alert, Avatar, Box, Button, Chip, CircularProgress, Divider, Grid, Paper, Stack, TextField, Typography } from "@mui/material";
import PhotoCameraOutlinedIcon from "@mui/icons-material/PhotoCameraOutlined";
import SettingsOutlinedIcon from "@mui/icons-material/SettingsOutlined";
import { useCurrentUser } from "../features/auth/hooks/useCurrentUser";
import { useProfile } from "../features/users/hooks/useProfile";
import { useDateFormatter } from "../features/users/hooks/useDateFormatter";
import { userService } from "../features/users/services/userService";
import type { UpdateProfileRequest, UserProfile } from "../features/users/types/user.types";
import { userDisplayName } from "../features/users/utils/userName";
import { prepareAvatar } from "../features/users/utils/avatar";
import { apiErrorMessage } from "../utils/apiError";
import { brandWash } from "../theme/theme";

export default function Profile() {
  const { profile, loading, error, reload } = useProfile();
  if (loading) return <Box sx={{ textAlign: "center", py: 8 }}><CircularProgress aria-label="Loading profile" /></Box>;
  if (error || !profile) return <Alert severity="error" action={<Button color="inherit" onClick={reload}>Retry</Button>}>{error || "Your profile could not be loaded."}</Alert>;
  return <ProfileEditor key={profile.id} profile={profile} />;
}

function ProfileEditor({ profile }: { profile: UserProfile }) {
  const user = useCurrentUser();
  const formatDate = useDateFormatter();
  const [current, setCurrent] = useState(profile);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const pending = useRef(false);
  const { register, handleSubmit, reset, formState: { errors, isDirty } } = useForm<UpdateProfileRequest>({
    defaultValues: { firstName: profile.firstName ?? "", lastName: profile.lastName ?? "", phone: profile.phone ?? "", bio: profile.bio ?? "" },
  });
  const name = userDisplayName(current, user?.username || "Your profile");

  const save = async (values: UpdateProfileRequest) => {
    if (pending.current) return;
    pending.current = true; setBusy(true); setError(null); setSuccess(null);
    try {
      const request = { firstName: values.firstName.trim(), lastName: values.lastName.trim(), phone: values.phone.trim(), bio: values.bio.trim() };
      const saved = await userService.updateProfile(request);
      setCurrent(saved); reset(request); setSuccess("Profile updated.");
    } catch (error) { setError(apiErrorMessage(error, "Unable to save your profile. Please try again.")); }
    finally { pending.current = false; setBusy(false); }
  };

  const changePhoto = async (event?: ChangeEvent<HTMLInputElement>) => {
    const file = event?.target.files?.[0];
    if (event) event.target.value = "";
    if (pending.current || (event && !file)) return;
    pending.current = true; setBusy(true); setError(null); setSuccess(null);
    try {
      let photo: string | null = null;
      if (file) {
        try { photo = await prepareAvatar(file); }
        catch (error) { setError(error instanceof Error ? error.message : "Unable to read this photo."); return; }
      }
      setCurrent(await userService.updateAvatar(photo));
      setSuccess(file ? "Profile photo updated." : "Profile photo removed.");
    } catch (error) { setError(apiErrorMessage(error, "Unable to update your photo. Please try again.")); }
    finally { pending.current = false; setBusy(false); }
  };

  return <Box sx={{ maxWidth: 1120, mx: "auto" }}>
    <Typography variant="h4" sx={{ fontWeight: 700 }}>My profile</Typography>
    <Typography color="text.secondary" sx={{ mt: 1, mb: 4 }}>Manage your personal details and profile photo.</Typography>
    {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}
    {success && <Alert severity="success" role="status" sx={{ mb: 3 }}>{success}</Alert>}
    <Grid container spacing={3}>
      <Grid size={{ xs: 12, md: 4 }}>
        <Paper variant="outlined" sx={{ overflow: "hidden", borderRadius: 3 }}>
          <Stack spacing={2} sx={{ p: 3, alignItems: "center", textAlign: "center", background: brandWash }}>
            <Avatar src={current.avatarUrl || undefined} alt={name} sx={{ width: 88, height: 88, fontSize: 32, border: "4px solid", borderColor: "background.paper" }}>{Array.from(name)[0]?.toUpperCase()}</Avatar>
            <Box sx={{ minWidth: 0, width: "100%" }}><Typography variant="h6" sx={{ fontWeight: 700, overflowWrap: "anywhere" }}>{name}</Typography>
              {user?.username && <Typography variant="body2" color="text.secondary" sx={{ overflowWrap: "anywhere" }}>@{user.username}</Typography>}</Box>
            <Chip size="small" color="secondary" variant="outlined" label={current.status === "ACTIVE" ? "Active member" : current.status} />
          </Stack>
          <Stack spacing={2} sx={{ p: 3 }}>
            <Button component="label" variant="outlined" startIcon={<PhotoCameraOutlinedIcon />} disabled={busy}>
              Change photo<input type="file" aria-label="Choose profile photo" accept="image/png,image/jpeg" hidden disabled={busy} onChange={event => { void changePhoto(event); }} />
            </Button>
            {current.avatarUrl && <Button disabled={busy} onClick={() => { void changePhoto(); }}>Remove photo</Button>}
            <Typography variant="caption" color="text.secondary">PNG or JPEG, up to 2 MB. Your photo is cropped to a square.</Typography>
            <Divider />
            <Typography variant="body2" color="text.secondary">Member since {formatDate(current.createdAt)}</Typography>
            <Button component={RouterLink} to="/dashboard/settings" startIcon={<SettingsOutlinedIcon />}>Account settings</Button>
          </Stack>
        </Paper>
      </Grid>
      <Grid size={{ xs: 12, md: 8 }}>
        <Paper component="form" variant="outlined" onSubmit={event => { void handleSubmit(save)(event); }} sx={{ p: { xs: 2.5, sm: 4 }, borderRadius: 3 }}>
          <Typography variant="h6" sx={{ mb: 1, fontWeight: 700 }}>Personal details</Typography>
          <Typography color="text.secondary" variant="body2" sx={{ mb: 3 }}>Your name appears in project teams and across your workspace.</Typography>
          <Stack spacing={3}>
            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField fullWidth required label="First name" autoComplete="given-name" disabled={busy} error={!!errors.firstName} helperText={errors.firstName?.message}
                slotProps={{ htmlInput: { maxLength: 100 } }} {...register("firstName", { validate: value => !!value.trim() || "Enter your first name." })} />
              <TextField fullWidth required label="Last name" autoComplete="family-name" disabled={busy} error={!!errors.lastName} helperText={errors.lastName?.message}
                slotProps={{ htmlInput: { maxLength: 100 } }} {...register("lastName", { validate: value => !!value.trim() || "Enter your last name." })} />
            </Stack>
            <TextField label="Phone number" type="tel" autoComplete="tel" disabled={busy} helperText="Optional. Visible only in your private profile."
              slotProps={{ htmlInput: { maxLength: 30 } }} {...register("phone")} />
            <TextField label="About you" multiline minRows={3} disabled={busy} helperText="A short introduction, up to 500 characters."
              slotProps={{ htmlInput: { maxLength: 500 } }} {...register("bio")} />
            <Divider />
            <TextField label="Sign-in email" value={current.email} slotProps={{ input: { readOnly: true } }} helperText="Contact your administrator to change your sign-in email." />
            <Stack direction="row" spacing={1} sx={{ justifyContent: "flex-end" }}>
              <Button disabled={busy || !isDirty} onClick={() => { reset(); setError(null); setSuccess(null); }}>Discard changes</Button>
              <Button type="submit" variant="contained" disabled={busy || !isDirty}>{busy ? "Saving..." : "Save profile"}</Button>
            </Stack>
          </Stack>
        </Paper>
      </Grid>
    </Grid>
  </Box>;
}
