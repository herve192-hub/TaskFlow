import { useEffect, useState } from "react";
import axios from "axios";

import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Stack,
  Typography,
} from "@mui/material";

import AddIcon from "@mui/icons-material/Add";
import FolderIcon from "@mui/icons-material/Folder";

import CreateProjectDialog from "../features/projects/components/CreateProjectDialog";
import { projectService } from "../features/projects/services/projectService";

import type {
  CreateProjectRequest,
  ProjectSummary,
} from "../features/projects/types/project.types";

export default function Projects() {
  const [projects, setProjects] = useState<ProjectSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // const loadProjects = async () => {
  //   try {
  //     setLoading(true);
  //     setError(null);

  //     const response = await projectService.getProjects();

  //     setProjects(response.content);
  //   } catch (error) {
  //     console.error("Unable to load projects:", error);

  //     setError("Unable to load projects.");
  //   } finally {
  //     setLoading(false);
  //   }
  // };

const loadProjects = async () => {
  try {
    setLoading(true);
    setError(null);

    const response = await projectService.getProjects();

    console.log("PROJECTS RESPONSE:", response);

    setProjects(response.content);
  } catch (error) {
    console.error("LOAD PROJECTS ERROR:", error);

    if (axios.isAxiosError(error)) {
      console.error("Status:", error.response?.status);
      console.error("Response:", error.response?.data);
      console.error("URL:", error.config?.url);

      setError(
        error.response?.data?.message ??
          `Unable to load projects (${error.response?.status ?? "network error"}).`
      );
    } else {
      console.error("Unexpected error:", error);
      setError("Unable to load projects.");
    }
  } finally {
    setLoading(false);
  }
};

  useEffect(() => {
    void loadProjects();
  }, []);

  const handleCreateProject = async (
    request: CreateProjectRequest
  ) => {
    try {
      setCreating(true);
      setError(null);

      await projectService.createProject(request);

      setDialogOpen(false);

      await loadProjects();
    } catch (error) {
      console.error("Unable to create project:", error);

      if (axios.isAxiosError(error)) {
        setError(
          error.response?.data?.message ??
            "Unable to create project."
        );
      } else {
        setError("Unable to create project.");
      }
    } finally {
      setCreating(false);
    }
  };

  return (
    <Box>
      <Stack
        direction={{ xs: "column", sm: "row" }}
        justifyContent="space-between"
        alignItems={{ xs: "stretch", sm: "center" }}
        spacing={2}
        sx={{ mb: 4 }}
      >
        <Box>
          <Typography variant="h4" fontWeight={700}>
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
                transition: "0.2s",
                "&:hover": {
                  boxShadow: 3,
                  transform: "translateY(-2px)",
                },
              }}
            >
              <CardContent>
                <Stack
                  direction="row"
                  justifyContent="space-between"
                  spacing={2}
                >
                  <Typography
                    variant="h6"
                    fontWeight={700}
                  >
                    {project.name}
                  </Typography>

                  <Chip
                    size="small"
                    label={project.status}
                  />
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
                  justifyContent="space-between"
                >
                  <Chip
                    size="small"
                    variant="outlined"
                    label={project.visibility}
                  />

                  <Typography
                    variant="body2"
                    color="text.secondary"
                  >
                    {project.memberCount} member
                    {project.memberCount === 1 ? "" : "s"}
                  </Typography>
                </Stack>
              </CardContent>
            </Card>
          ))}
        </Box>
      )}

      <CreateProjectDialog
        open={dialogOpen}
        loading={creating}
        onClose={() => setDialogOpen(false)}
        onCreate={handleCreateProject}
      />
    </Box>
  );
}