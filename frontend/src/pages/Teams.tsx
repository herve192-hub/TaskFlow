import { useEffect, useState } from "react";
import { Link as RouterLink } from "react-router-dom";
import { Alert, Box, Button, Card, CardActions, CardContent, CircularProgress, Pagination, Stack, Typography } from "@mui/material";
import GroupIcon from "@mui/icons-material/Group";
import FolderIcon from "@mui/icons-material/Folder";
import { projectService } from "../features/projects/services/projectService";
import type { PageResponse, ProjectSummary } from "../features/projects/types/project.types";
import { apiErrorMessage } from "../utils/apiError";

export default function Teams() {
  const [result, setResult] = useState<PageResponse<ProjectSummary> | null>(null);
  const [page, setPage] = useState(0);
  const [revision, setRevision] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const teams = await projectService.getProjects(page);
        if (active) setResult(teams);
      } catch (error) {
        if (active) setError(apiErrorMessage(error, "Unable to load your project teams. Please retry."));
      } finally { if (active) setLoading(false); }
    };
    void load();
    return () => { active = false; };
  }, [page, revision]);

  return <Box>
    <Stack direction={{ xs: "column", sm: "row" }} spacing={2} sx={{ mb: 4, justifyContent: "space-between", alignItems: { sm: "center" } }}>
      <Box>
        <Typography variant="h4" sx={{ fontWeight: 700 }}>Teams</Typography>
        <Typography color="text.secondary">Each project has its own team. Choose a project to view or manage its members.</Typography>
      </Box>
      <Button component={RouterLink} to="/dashboard/projects" variant="outlined" startIcon={<FolderIcon />}>Projects</Button>
    </Stack>
    {loading ? <Box sx={{ textAlign: "center", py: 8 }}><CircularProgress aria-label="Loading teams" /></Box>
      : error ? <Alert severity="error" action={<Button color="inherit" onClick={() => { setLoading(true); setRevision((value) => value + 1); }}>Retry</Button>}>{error}</Alert>
      : !result?.content.length ? <Box sx={{ textAlign: "center", py: 8, border: "1px dashed", borderColor: "divider", borderRadius: 3 }}>
        <GroupIcon color="disabled" sx={{ fontSize: 52, mb: 2 }} />
        <Typography variant="h6">No project teams yet</Typography>
        <Typography color="text.secondary" sx={{ mb: 3 }}>Create a project to form your first team.</Typography>
        <Button component={RouterLink} to="/dashboard/projects" variant="contained">Go to projects</Button>
      </Box> : <Stack spacing={3}>
        <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", md: "repeat(2, 1fr)", lg: "repeat(3, 1fr)" }, gap: 3 }}>
          {result.content.map((project) => <Card key={project.id} variant="outlined" sx={{ display: "flex", flexDirection: "column" }}>
            <CardContent sx={{ flex: 1 }}>
              <Typography variant="h6" sx={{ fontWeight: 700 }}>{project.name}</Typography>
              <Typography color="text.secondary" sx={{ mt: 1, mb: 2 }}>{project.description || "No description provided."}</Typography>
              <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
                <GroupIcon color="action" fontSize="small" />
                <Typography variant="body2" color="text.secondary">{project.memberCount} member{project.memberCount === 1 ? "" : "s"}</Typography>
              </Stack>
            </CardContent>
            <CardActions sx={{ px: 2, pb: 2 }}>
              <Button component={RouterLink} to={`/dashboard/projects/${encodeURIComponent(project.id)}/members`}
                aria-label={`View team for ${project.name}`}>View team</Button>
            </CardActions>
          </Card>)}
        </Box>
        {result.totalPages > 1 && <Pagination count={result.totalPages} page={page + 1} aria-label="Project team pages"
          onChange={(_, value) => { setLoading(true); setPage(value - 1); }} />}
      </Stack>}
  </Box>;
}
