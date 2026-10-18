import { useRef, useState } from "react";
import type { FormEvent } from "react";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { Alert, Autocomplete, Box, Button, CircularProgress, Divider, FormControlLabel, MenuItem, Paper, Stack, Switch, TextField, Typography } from "@mui/material";
import PersonOutlineIcon from "@mui/icons-material/PersonOutlineOutlined";
import { useProfile } from "../features/users/hooks/useProfile";
import { useCurrentUser } from "../features/auth/hooks/useCurrentUser";
import { authService } from "../features/auth/services/authService";
import { userService } from "../features/users/services/userService";
import type { UserPreferences } from "../features/users/types/user.types";
import { defaultPreferences, formatAccountDate } from "../features/users/utils/preferences";
import { apiErrorMessage } from "../utils/apiError";

const timeZones = [...new Set(["UTC", ...Intl.supportedValuesOf("timeZone")])].sort();
const startPages = { DASHBOARD: "Dashboard", PROJECTS: "Projects", TASKS: "Tasks", TEAMS: "Teams" };

export default function Settings() {
  const { profile, loading, error, reload } = useProfile();
  if (loading) return <Box sx={{ textAlign: "center", py: 8 }}><CircularProgress aria-label="Loading settings" /></Box>;
  if (error || !profile) return <Alert severity="error" action={<Button color="inherit" onClick={reload}>Retry</Button>}>{error || "Your settings could not be loaded."}</Alert>;
  return <SettingsEditor key={profile.id} initial={profile.preferences || defaultPreferences} />;
}

function SettingsEditor({ initial }: { initial: UserPreferences }) {
  const user = useCurrentUser();
  const navigate = useNavigate();
  const [preferences, setPreferences] = useState(initial);
  const [saved, setSaved] = useState(initial);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const pending = useRef(false);
  const dirty = preferences.timeZone !== saved.timeZone || preferences.dateFormat !== saved.dateFormat
    || preferences.startPage !== saved.startPage || preferences.use24HourTime !== saved.use24HourTime;
  const update = (patch: Partial<UserPreferences>) => { setSuccess(false); setPreferences(value => ({ ...value, ...patch })); };
  const save = async (event: FormEvent) => {
    event.preventDefault();
    if (pending.current) return;
    pending.current = true; setBusy(true); setError(null); setSuccess(false);
    try {
      const profile = await userService.updatePreferences(preferences);
      setPreferences(profile.preferences); setSaved(profile.preferences); setSuccess(true);
    } catch (error) { setError(apiErrorMessage(error, "Unable to save settings. Please try again.")); }
    finally { pending.current = false; setBusy(false); }
  };
  const signOut = async () => {
    if (pending.current) return;
    pending.current = true; setBusy(true); setError(null);
    try { await authService.logout(); navigate("/login", { replace: true }); }
    catch { setError("Sign-out could not reach the server. Please retry."); }
    finally { pending.current = false; setBusy(false); }
  };
  return <Box sx={{ maxWidth: 960, mx: "auto" }}>
    <Typography variant="h4" sx={{ fontWeight: 700 }}>Settings</Typography>
    <Typography color="text.secondary" sx={{ mt: 1, mb: 4 }}>Make TaskFlow work the way you do.</Typography>
    <Stack spacing={3}>
      {error && <Alert severity="error">{error}</Alert>}
      {success && <Alert severity="success" role="status">Settings saved.</Alert>}
      <Paper component="form" variant="outlined" onSubmit={save} sx={{ p: { xs: 2.5, sm: 4 }, borderRadius: 3 }}>
        <Typography variant="h6" sx={{ fontWeight: 700 }}>Workspace preferences</Typography>
        <Typography color="text.secondary" variant="body2" sx={{ mt: 1, mb: 3 }}>Saved to your account and used wherever you sign in.</Typography>
        <Stack spacing={3}>
          <Autocomplete options={timeZones.includes(preferences.timeZone) ? timeZones : [preferences.timeZone, ...timeZones]} value={preferences.timeZone} disableClearable disabled={busy}
            onChange={(_, timeZone) => update({ timeZone })}
            renderInput={params => <TextField {...params} label="Time zone" helperText="Used for deadlines and dates throughout your workspace." />} />
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
            <TextField fullWidth select label="Date format" value={preferences.dateFormat} disabled={busy}
              onChange={event => update({ dateFormat: event.target.value as UserPreferences["dateFormat"] })}>
              <MenuItem value="MONTH_DAY_YEAR">Month / day / year</MenuItem>
              <MenuItem value="DAY_MONTH_YEAR">Day / month / year</MenuItem>
              <MenuItem value="YEAR_MONTH_DAY">Year - month - day</MenuItem>
            </TextField>
            <TextField fullWidth select label="Page after sign-in" value={preferences.startPage} disabled={busy}
              onChange={event => update({ startPage: event.target.value as UserPreferences["startPage"] })}>
              {Object.entries(startPages).map(([value, label]) => <MenuItem key={value} value={value}>{label}</MenuItem>)}
            </TextField>
          </Stack>
          <FormControlLabel control={<Switch checked={preferences.use24HourTime} disabled={busy} onChange={(_, checked) => update({ use24HourTime: checked })} />} label="Use 24-hour time" />
          <Box sx={{ p: 2, bgcolor: "primary.light", borderRadius: 2 }}>
            <Typography variant="body2" sx={{ fontWeight: 600 }}>Date and time preview</Typography>
            <Typography variant="body2" sx={{ mt: 0.5 }}>{formatAccountDate(new Date().toISOString(), preferences, true)} · {preferences.timeZone}</Typography>
          </Box>
          <Divider />
          <Stack direction={{ xs: "column", sm: "row" }} spacing={1} sx={{ justifyContent: "space-between" }}>
            <Button disabled={busy} onClick={() => update(defaultPreferences)}>Reset to defaults</Button>
            <Stack direction="row" spacing={1} sx={{ justifyContent: "flex-end" }}>
              <Button disabled={busy || !dirty} onClick={() => { setPreferences(saved); setError(null); setSuccess(false); }}>Discard changes</Button>
              <Button type="submit" variant="contained" disabled={busy || !dirty}>{busy ? "Saving..." : "Save settings"}</Button>
            </Stack>
          </Stack>
        </Stack>
      </Paper>
      <Paper variant="outlined" sx={{ p: { xs: 2.5, sm: 4 }, borderRadius: 3 }}>
        <Typography variant="h6" sx={{ fontWeight: 700, mb: 1 }}>Your account</Typography>
        <Typography color="text.secondary" sx={{ overflowWrap: "anywhere", mb: 3 }}>Signed in as {user?.email || user?.username}.</Typography>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
          <Button component={RouterLink} to="/dashboard/profile" startIcon={<PersonOutlineIcon />} variant="outlined">Edit profile</Button>
          <Button color="error" disabled={busy} onClick={() => { void signOut(); }}>Sign out of this browser</Button>
        </Stack>
      </Paper>
    </Stack>
  </Box>;
}
