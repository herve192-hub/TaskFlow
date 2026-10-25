/**
 * Caller-visible project landing page. Fetches the default server page, displays
 * membership counts/metadata, and links to per-project tasks and teams. Successful
 * creation increments a revision to reload authoritative results; failures remain
 * in the dialog. Effect cleanup ignores stale responses. This screen currently
 * shows only the default page; Teams and workspace aggregation handle pagination.
 */

import { useEffect, useState } from "react";
import { apiErrorMessage } from "../utils/apiError";
import { Link as RouterLink } from "react-router-dom";

import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CardActionArea,
  CardActions,
  CircularProgress,
  Stack,
  Typography,
} from "@mui/material";

import AddIcon from "@mui/icons-material/Add";
import FolderIcon from "@mui/icons-material/Folder";
import GroupIcon from "@mui/icons-material/Group";
import WorkBadge from "../components/WorkBadge";

import CreateProjectDialog from "../features/projects/components/CreateProjectDialog";
import { projectService } from "../features/projects/services/projectService";

import type {
  CreateProjectRequest,
  ProjectSummary,
} from "../features/projects/types/project.types";

/** Show the caller's default project page and refresh it after successful project creation. */
export default function Projects() {
  const [projects, setProjects] = useState<ProjectSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [revision, setRevision] = useState(0);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);


  useEffect(() => {
    // Ignore the initial request's late result when this route unmounts.
    let active = true;

    const fetchProjects = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await projectService.getProjects();

        if (!active) {
          return;
        }

        setProjects(response.content);
      } catch (error) {
        if (!active) {
          return;
        }

        setError(apiErrorMessage(error, "Unable to load projects. Please try again."));
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    void fetchProjects();

    return () => {
      active = false;
    };
  }, [revision]);

  const handleCreateProject = async (request: CreateProjectRequest) => {
    await projectService.createProject(request);
    setLoading(true);
    setRevision(value => value + 1);
  };

  return (
    <Box>
      <Stack
        direction={{ xs: "column", sm: "row" }}
        spacing={2}
        sx={{
          mb: 4,
          justifyContent: "space-between",
          alignItems: { xs: "stretch", sm: "center" },
        }}
      >
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700 }}>
            Projects
          </Typography>

          <Typography color="text.secondary">
            Manage your projects and their work.
          </Typography>
        </Box>

        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setDialogOpen(true)}
        >
          Create Project
        </Button>
      </Stack>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {loading ? (
        <Box
          sx={{
            display: "flex",
            justifyContent: "center",
            py: 8,
          }}
        >
          <CircularProgress />
        </Box>
      ) : projects.length === 0 ? (
        <Box
          sx={{
            textAlign: "center",
            py: 8,
            border: "1px dashed",
            borderColor: "divider",
            borderRadius: 3,
          }}
        >
          <FolderIcon
            color="disabled"
            sx={{ fontSize: 52, mb: 2 }}
          />

          <Typography variant="h6">
            No projects yet
          </Typography>

          <Typography
            color="text.secondary"
            sx={{ mb: 3 }}
          >
            Create your first project to start organizing tasks.
          </Typography>

          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setDialogOpen(true)}
          >
            Create Project
          </Button>
        </Box>
      ) : (
        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: {
              xs: "1fr",
              md: "repeat(2, 1fr)",
              lg: "repeat(3, 1fr)",
            },
            gap: 3,
          }}
        >
          {projects.map((project) => (
            <Card
              key={project.id}
              variant="outlined"
              sx={{
                height: "100%",
                display: "flex",
                flexDirection: "column",
                transition: "0.2s",
                "&:hover": {
                  boxShadow: 3,
                  transform: "translateY(-2px)",
                },
              }}
            >
              <CardActionArea
                component={RouterLink}
                to={`/dashboard/projects/${encodeURIComponent(project.id)}/tasks`}
                aria-label={`View tasks for ${project.name}`}
                sx={{ flex: 1 }}
              >
              <CardContent>
                <Stack
                  direction="row"
                  sx={{ justifyContent: "space-between" }}
                  spacing={2}
                >
                  <Typography
                    variant="h6"
                    sx={{ fontWeight: 700 }}
                  >
                    {project.name}
                  </Typography>

                  <WorkBadge kind="projectStatus" value={project.status} />
                </Stack>

                <Typography
                  color="text.secondary"
                  sx={{
                    mt: 1.5,
                    mb: 3,
                    minHeight: 48,
                  }}
                >
                  {project.description ||
                    "No description provided."}
                </Typography>

                <Stack
                  direction="row"
                  sx={{ justifyContent: "space-between" }}
                >
                  <WorkBadge kind="visibility" value={project.visibility} />

                  <Typography
                    variant="body2"
                    color="text.secondary"
                  >
                    {project.memberCount} member
                    {project.memberCount === 1 ? "" : "s"}
                  </Typography>
                </Stack>
                <Typography color="primary" variant="body2" sx={{ mt: 2 }}>View tasks →</Typography>
              </CardContent>
              </CardActionArea>
              <CardActions sx={{ px: 2, pb: 2 }}>
                <Button component={RouterLink} startIcon={<GroupIcon />}
                  to={`/dashboard/projects/${encodeURIComponent(project.id)}/members`}
                  aria-label={`View team for ${project.name}`}>View team</Button>
              </CardActions>
            </Card>
          ))}
        </Box>
      )}

      {dialogOpen && <CreateProjectDialog
        open
        onClose={() => setDialogOpen(false)}
        onCreate={handleCreateProject}
      />}
    </Box>
  );
}
