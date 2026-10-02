import { useEffect, useRef, useState } from "react";
import type { FormEvent } from "react";
import {
  Alert, Avatar, Box, Button, CircularProgress, Dialog, DialogActions, DialogContent,
  DialogTitle, List, ListItemButton, ListItemAvatar, ListItemText, MenuItem,
  Pagination, Radio, Stack, TextField, Typography,
} from "@mui/material";
import { projectService } from "../services/projectService";
import { userService } from "../../users/services/userService";
import { ProjectRole } from "../types/project.types";
import type { AddProjectMemberRequest } from "../types/project.types";
import type { UserSearchPage, UserSummary } from "../../users/types/user.types";
import { assignableProjectRoles, projectRoleDescriptions, projectRoleLabels } from "../utils/projectRoles";
import { apiErrorMessage } from "../../../utils/apiError";
import { userDisplayName } from "../../users/utils/userName";

interface Props {
  projectId: string;
  onClose: () => void;
  onAdd: (request: AddProjectMemberRequest) => Promise<void>;
}

export default function AddProjectMemberDialog({ projectId, onClose, onAdd }: Props) {
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [result, setResult] = useState<UserSearchPage | null>(null);
  const [existingUserIds, setExistingUserIds] = useState<Set<string> | null>(null);
  const [selected, setSelected] = useState<UserSummary | null>(null);
  const [role, setRole] = useState(ProjectRole.MEMBER);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [membershipError, setMembershipError] = useState<string | null>(null);
  const [revision, setRevision] = useState(0);
  const pending = useRef(false);

  useEffect(() => {
    let active = true;
    void projectService.getAllProjectMembers(projectId).then((members) => {
      if (active) {
        setExistingUserIds(new Set(members.map((member) => member.userId)));
        setMembershipError(null);
      }
    }).catch((error: unknown) => {
      if (active) setMembershipError(apiErrorMessage(error, "Unable to check existing members. Please retry."));
    });
    return () => { active = false; };
  }, [projectId, revision]);

  useEffect(() => {
    let active = true;
    const timer = window.setTimeout(() => {
      setLoading(true);
      setError(null);
      void userService.searchUsers(query, page).then((response) => {
        if (active) setResult(response);
      }).catch((error: unknown) => {
        if (active) setError(apiErrorMessage(error, "Unable to find people. Please retry."));
      }).finally(() => { if (active) setLoading(false); });
    }, 300);
    return () => { active = false; window.clearTimeout(timer); };
  }, [query, page, revision]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (pending.current || !selected || !existingUserIds || existingUserIds.has(selected.authUserId) || membershipError) return;
    pending.current = true;
    setSaving(true);
    setError(null);
    try {
      await onAdd({ userId: selected.authUserId, role });
      onClose();
    } catch (error) {
      setError(apiErrorMessage(error, "Unable to add this member. Please try again."));
    } finally {
      pending.current = false;
      setSaving(false);
    }
  };

  const availableUsers = result?.content.filter((user) =>
    user.status === "ACTIVE" && user.authUserId && !existingUserIds?.has(user.authUserId)) ?? [];

  return (
    <Dialog open fullWidth maxWidth="sm" onClose={() => { if (!pending.current) onClose(); }} aria-labelledby="add-member-title">
      <form onSubmit={handleSubmit}>
        <DialogTitle id="add-member-title">Add a team member</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <Typography color="text.secondary">Choose a registered TaskFlow user to join this project.</Typography>
            {(error || membershipError) && <Alert severity="error" action={
              <Button color="inherit" disabled={saving} onClick={() => { setLoading(true); setRevision((value) => value + 1); }}>Retry</Button>
            }>{membershipError || error}</Alert>}
            <TextField label="Search by name or email" autoFocus fullWidth value={query} disabled={saving}
              slotProps={{ htmlInput: { maxLength: 100 } }}
              onChange={(event) => { setQuery(event.target.value); setPage(0); setLoading(true); }} />
            {loading || (!existingUserIds && !membershipError) ? <Box sx={{ textAlign: "center", py: 3 }}>
              <CircularProgress size={28} aria-label="Finding available people" />
            </Box> : !error && !membershipError && <>
              <List aria-label="Available people" sx={{ maxHeight: 300, overflow: "auto" }}>
                {availableUsers.map((user) => <ListItemButton key={user.authUserId} disabled={saving}
                  selected={selected?.authUserId === user.authUserId}
                  onClick={() => setSelected(user)} aria-label={`Select ${userDisplayName(user)}`}>
                  <ListItemAvatar><Avatar src={user.avatarUrl || undefined}>{userDisplayName(user) === "Name unavailable" ? "?" : userDisplayName(user).slice(0, 1).toUpperCase()}</Avatar></ListItemAvatar>
                  <ListItemText primary={userDisplayName(user)} secondary={user.email} />
                  <Radio checked={selected?.authUserId === user.authUserId} tabIndex={-1}
                    slotProps={{ input: { "aria-label": userDisplayName(user) } }} />
                </ListItemButton>)}
              </List>
              {!availableUsers.length && <Typography color="text.secondary">
                No available people on this page. Try another name, email, or page.
              </Typography>}
              {result && result.totalPages > 1 && <Pagination count={result.totalPages} page={page + 1}
                disabled={saving} aria-label="People pages"
                onChange={(_, value) => { setPage(value - 1); setLoading(true); }} />}
            </>}
            {selected && <Alert severity="info">Selected: {userDisplayName(selected)}</Alert>}
            <TextField select label="Project role" value={role} disabled={saving}
              helperText={projectRoleDescriptions[role]} onChange={(event) => setRole(event.target.value as ProjectRole)}>
              {assignableProjectRoles.map((value) => <MenuItem key={value} value={value}>{projectRoleLabels[value]}</MenuItem>)}
            </TextField>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button onClick={onClose} disabled={saving}>Cancel</Button>
          <Button type="submit" variant="contained" disabled={saving || !selected || !existingUserIds || !!membershipError || (!!selected && existingUserIds.has(selected.authUserId))}>
            {saving ? "Adding..." : "Add member"}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
