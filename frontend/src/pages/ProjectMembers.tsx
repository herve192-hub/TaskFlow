import { useEffect, useRef, useState } from "react";
import { Link as RouterLink, useNavigate, useParams } from "react-router-dom";
import {
  Alert, Avatar, Box, Button, Card, CardContent, Chip, CircularProgress, Dialog,
  DialogActions, DialogContent, DialogTitle, MenuItem, Pagination, Stack, TextField, Typography,
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import PersonAddIcon from "@mui/icons-material/PersonAdd";
import AddProjectMemberDialog from "../features/projects/components/AddProjectMemberDialog";
import { projectService } from "../features/projects/services/projectService";
import { userService } from "../features/users/services/userService";
import { userDisplayName } from "../features/users/utils/userName";
import { authService } from "../features/auth/services/authService";
import { ProjectRole } from "../features/projects/types/project.types";
import type {
  AddProjectMemberRequest, PageResponse, ProjectAccessResponse, ProjectMemberResponse, ProjectResponse,
} from "../features/projects/types/project.types";
import type { UserSummary } from "../features/users/types/user.types";
import { assignableProjectRoles, projectRoleLabels } from "../features/projects/utils/projectRoles";
import { apiErrorMessage } from "../utils/apiError";

export default function ProjectMembers() {
  const { projectId } = useParams<{ projectId: string }>();
  return projectId ? <ProjectTeam key={projectId} projectId={projectId} /> : <Alert severity="error">Project ID is required.</Alert>;
}

function ProjectTeam({ projectId }: { projectId: string }) {
  const navigate = useNavigate();
  const [project, setProject] = useState<ProjectResponse | null>(null);
  const [access, setAccess] = useState<ProjectAccessResponse | null>(null);
  const [result, setResult] = useState<PageResponse<ProjectMemberResponse> | null>(null);
  const [profiles, setProfiles] = useState<Map<string, UserSummary>>(new Map());
  const [sessionUser] = useState(() => authService.getCurrentUser());
  const [page, setPage] = useState(0);
  const [revision, setRevision] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [profileWarning, setProfileWarning] = useState(false);
  const [success, setSuccess] = useState<string | null>(null);
  const [addOpen, setAddOpen] = useState(false);
  const [removing, setRemoving] = useState<ProjectMemberResponse | null>(null);
  const pending = useRef(false);

  useEffect(() => {
    let active = true;
    const load = async () => {
      setLoading(true);
      setLoadError(null);
      setProfileWarning(false);
      try {
        const [details, permissions, members] = await Promise.all([
          projectService.getProject(projectId), projectService.getProjectAccess(projectId),
          projectService.getProjectMembers(projectId, page),
        ]);
        if (!active) return;
        setProject(details);
        setAccess(permissions);
        setResult(members);
        try {
          const users = await userService.lookupUsers(members.content.map((member) => member.userId));
          if (active) setProfiles(new Map(users.map((user) => [user.authUserId, user])));
        } catch {
          if (active) { setProfiles(new Map()); setProfileWarning(true); }
        }
      } catch (error) {
        if (active) setLoadError(apiErrorMessage(error, "Unable to load this project team. Please retry."));
      } finally {
        if (active) setLoading(false);
      }
    };
    void load();
    return () => { active = false; };
  }, [projectId, page, revision]);

  const reload = () => { setLoading(true); setRevision((value) => value + 1); };

  const handleAdd = async (request: AddProjectMemberRequest) => {
    await projectService.addProjectMember(projectId, request);
    setError(null);
    setSuccess("Team member added.");
    reload();
  };

  const changeRole = async (member: ProjectMemberResponse, role: ProjectRole) => {
    if (pending.current || role === member.role) return;
    pending.current = true;
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await projectService.updateProjectMember(projectId, member.id, { role });
      setSuccess("Member role updated.");
      reload();
    } catch (error) {
      setError(apiErrorMessage(error, "Unable to update this member's role."));
    } finally { pending.current = false; setSaving(false); }
  };

  const removeMember = async () => {
    if (pending.current || !removing) return;
    pending.current = true;
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await projectService.removeProjectMember(projectId, removing.id);
      setRemoving(null);
      if (removing.userId === access?.userId) {
        navigate("/dashboard/teams", { replace: true });
        return;
      }
      setSuccess("Team member removed.");
      if (result?.content.length === 1 && page > 0) setPage(page - 1);
      reload();
    } catch (error) {
      setError(apiErrorMessage(error, "Unable to remove this member."));
    } finally { pending.current = false; setSaving(false); }
  };

  const nameFor = (member: ProjectMemberResponse) => {
    const profile = profiles.get(member.userId);
    return userDisplayName(profile, "")
      || (sessionUser?.id === member.userId ? userDisplayName(sessionUser, "") : "")
      || "Name unavailable";
  };

  const namesUnavailable = profileWarning || result?.content.some((member) => nameFor(member) === "Name unavailable");

  return (
    <Box>
      <Button component={RouterLink} to="/dashboard/teams" startIcon={<ArrowBackIcon />} sx={{ mb: 2 }}>Back to teams</Button>
      <Stack direction={{ xs: "column", sm: "row" }} spacing={2} sx={{ mb: 4, justifyContent: "space-between", alignItems: { sm: "center" } }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700 }}>{project ? `${project.name} team` : "Project team"}</Typography>
          <Typography color="text.secondary">Build your team and manage project membership.</Typography>
        </Box>
        <Stack direction="row" spacing={1}>
          <Button component={RouterLink} to={`/dashboard/projects/${encodeURIComponent(projectId)}/tasks`} variant="outlined">View tasks</Button>
          {access?.canManageMembers && !loadError && <Button variant="contained" startIcon={<PersonAddIcon />}
            disabled={loading || saving} onClick={() => setAddOpen(true)}>Add member</Button>}
        </Stack>
      </Stack>
      {success && <Alert severity="success" role="status" sx={{ mb: 2 }}>{success}</Alert>}
      {error && !removing && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {loading ? <Box sx={{ textAlign: "center", py: 8 }}><CircularProgress aria-label="Loading project team" /></Box>
        : loadError ? <Alert severity="error" action={<Button color="inherit" onClick={reload}>Retry</Button>}>{loadError}</Alert>
        : <Stack spacing={2}>
          {namesUnavailable && <Alert severity="warning" action={<Button color="inherit" disabled={saving} onClick={reload}>Retry</Button>}>
            Some member names could not be loaded. Please try again.
          </Alert>}
          {!access?.canManageMembers && <Alert severity="info">Your project role allows you to view the team. Owners and admins manage membership.</Alert>}
          <Typography color="text.secondary">{result?.totalElements ?? 0} team member{result?.totalElements === 1 ? "" : "s"}</Typography>
          {result?.content.map((member) => {
            const profile = profiles.get(member.userId);
            const owner = member.role === ProjectRole.OWNER;
            return <Card key={member.id} variant="outlined"><CardContent>
              <Stack direction={{ xs: "column", sm: "row" }} spacing={2} sx={{ justifyContent: "space-between", alignItems: { sm: "center" } }}>
                <Stack direction="row" spacing={2} sx={{ alignItems: "center", minWidth: 0 }}>
                  <Avatar src={profile?.avatarUrl || undefined}>{nameFor(member) === "Name unavailable" ? "?" : nameFor(member).slice(0, 1).toUpperCase()}</Avatar>
                  <Box sx={{ minWidth: 0 }}>
                    <Typography sx={{ fontWeight: 600, overflowWrap: "anywhere" }}>{nameFor(member)}{member.userId === access?.userId ? " (you)" : ""}</Typography>
                    {profile?.email && <Typography variant="body2" color="text.secondary" sx={{ overflowWrap: "anywhere" }}>{profile.email}</Typography>}
                    {member.joinedAt && <Typography variant="caption" color="text.secondary">Joined {new Date(member.joinedAt).toLocaleDateString()}</Typography>}
                  </Box>
                </Stack>
                {access?.canManageMembers && !owner ? <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
                  <TextField select label="Role" size="small" value={member.role} disabled={saving}
                    sx={{ minWidth: 130 }} slotProps={{ select: { inputProps: { "aria-label": `Role for ${nameFor(member)}` } } }}
                    onChange={(event) => { void changeRole(member, event.target.value as ProjectRole); }}>
                    {assignableProjectRoles.map((role) => <MenuItem key={role} value={role}>{projectRoleLabels[role]}</MenuItem>)}
                  </TextField>
                  <Button color="error" disabled={saving} aria-label={`Remove ${nameFor(member)}`} onClick={() => { setError(null); setRemoving(member); }}>Remove</Button>
                </Stack> : <Chip label={projectRoleLabels[member.role]} color={owner ? "primary" : "default"} variant="outlined" />}
              </Stack>
            </CardContent></Card>;
          })}
          {result && result.totalPages > 1 && <Pagination count={result.totalPages} page={page + 1} disabled={saving}
            aria-label="Team member pages" onChange={(_, value) => { setLoading(true); setPage(value - 1); }} />}
        </Stack>}
      {addOpen && <AddProjectMemberDialog projectId={projectId} onClose={() => setAddOpen(false)} onAdd={handleAdd} />}
      <Dialog open={!!removing} onClose={() => { if (!pending.current) { setRemoving(null); setError(null); } }} aria-labelledby="remove-member-title">
        <DialogTitle id="remove-member-title">Remove team member?</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <Typography>{removing ? nameFor(removing) : "This member"} will lose access to this project and its tasks.</Typography>
        </DialogContent>
        <DialogActions>
          <Button disabled={saving} onClick={() => { setRemoving(null); setError(null); }}>Cancel</Button>
          <Button color="error" variant="contained" disabled={saving} onClick={() => { void removeMember(); }}>{saving ? "Removing..." : "Remove member"}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
