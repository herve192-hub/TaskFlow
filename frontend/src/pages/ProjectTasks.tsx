import { useEffect, useRef, useState } from "react";
import axios from "axios";
import { Link as RouterLink, useParams } from "react-router-dom";
import { Alert, Box, Button, Card, CardContent, Chip, CircularProgress, Pagination, Stack, Typography, TextField, MenuItem } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import CreateTaskDialog from "../features/tasks/components/CreateTaskDialog";
import { taskService } from "../features/tasks/services/taskService";
import type { CreateTaskRequest, PageResponse, TaskSummaryResponse } from "../features/tasks/types/task.types";

import { TaskStatus } from "../features/tasks/types/task.types";

export default function ProjectTasks() {
  const { projectId } = useParams<{ projectId: string }>();
  return projectId ? <ProjectTaskList key={projectId} projectId={projectId} /> : <Alert severity="error">Project ID is required.</Alert>;
}

function ProjectTaskList({ projectId }: { projectId: string }) {
  const [result, setResult] = useState<PageResponse<TaskSummaryResponse> | null>(null);
  const [page, setPage] = useState(0);
  const [revision, setRevision] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);

  useEffect(() => {
    let active = true;
    const loadTasks = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await taskService.getProjectTasks(projectId, page);
        if (active) setResult(response);
      } catch (error) {
        if (active) setError(axios.isAxiosError(error) && typeof error.response?.data?.message === "string"
          ? error.response.data.message : "Unable to load tasks. Please try again.");
      } finally {
        if (active) setLoading(false);
      }
    };
    void loadTasks();
    return () => { active = false; };
  }, [projectId, page, revision]);

  const handleCreate = async (request: CreateTaskRequest) => {
    await taskService.createTask(request);
    setLoading(true);
    setPage(0);
    setRevision((value) => value + 1);
  };

  return (
    <Box>
      <Button component={RouterLink} to="/dashboard/projects" startIcon={<ArrowBackIcon />} sx={{ mb: 2 }}>Back to projects</Button>
      <Stack direction={{ xs: "column", sm: "row" }} spacing={2} sx={{ mb: 4, justifyContent: "space-between", alignItems: { sm: "center" } }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700 }}>Project Tasks</Typography>
          <Typography color="text.secondary">View and create tasks for this project.</Typography>
        </Box>
        <Stack direction="row" spacing={1}>
          <Button component={RouterLink} to={`/dashboard/projects/${encodeURIComponent(projectId)}/members`} variant="outlined">View team</Button>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Create Task</Button>
        </Stack>
      </Stack>
      {loading ? <Box sx={{ display: "flex", justifyContent: "center", py: 8 }}><CircularProgress aria-label="Loading tasks" /></Box>
        : error ? <Alert severity="error" action={<Button color="inherit" onClick={() => { setLoading(true); setRevision((value) => value + 1); }}>Retry</Button>}>{error}</Alert>
        : !result?.content.length ? <Box sx={{ textAlign: "center", py: 8, border: "1px dashed", borderColor: "divider", borderRadius: 3 }}>
          <Typography variant="h6">No tasks yet</Typography>
          <Typography color="text.secondary" sx={{ mb: 3 }}>Create a task to start working on this project.</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Create Task</Button>
        </Box> : <Stack spacing={2}>
          <Typography color="text.secondary">{result.totalElements} task{result.totalElements === 1 ? "" : "s"}</Typography>
          {result.content.map((task) => <Card key={task.id} variant="outlined">
            <CardContent>
              <Typography variant="h6" sx={{ overflowWrap: "anywhere" }}>{task.title}</Typography>
              <Stack direction="row" spacing={1} useFlexGap sx={{ mt: 1, flexWrap: "wrap" }}>
                <Chip size="small" label={task.status.replaceAll("_", " ")} />
                <Chip size="small" variant="outlined" label={task.priority} />
                <Chip size="small" variant="outlined" label={task.type} />
              </Stack>
              <TaskStatusControl task={task} onUpdated={(updated) => setResult((current) => current ? {
                ...current, content: current.content.map((item) => item.id === updated.id ? updated : item),
              } : current)} />
              {task.dueDate && <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>Due {new Date(task.dueDate).toLocaleString()}</Typography>}
            </CardContent>
          </Card>)}
          {result.totalPages > 1 && <Pagination count={result.totalPages} page={page + 1} onChange={(_, value) => { setLoading(true); setPage(value - 1); }} aria-label="Task pages" />}
        </Stack>}
      {dialogOpen && <CreateTaskDialog open projectId={projectId} onClose={() => setDialogOpen(false)} onCreate={handleCreate} />}
    </Box>
  );
}

const transitions: Record<TaskStatus, TaskStatus[]> = {
  TODO: [TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED, TaskStatus.CANCELLED],
  IN_PROGRESS: [TaskStatus.TODO, TaskStatus.IN_REVIEW, TaskStatus.BLOCKED, TaskStatus.CANCELLED],
  IN_REVIEW: [TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED, TaskStatus.COMPLETED, TaskStatus.CANCELLED],
  BLOCKED: [TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED],
  COMPLETED: [TaskStatus.IN_PROGRESS],
  CANCELLED: [TaskStatus.TODO],
};

function TaskStatusControl({ task, onUpdated }: {
  task: TaskSummaryResponse;
  onUpdated: (task: TaskSummaryResponse) => void;
}) {
  const pending = useRef(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);
  const changeStatus = async (status: TaskStatus) => {
    if (pending.current || status === task.status) return;
    pending.current = true;
    setSaving(true);
    setError(null);
    setSaved(false);
    try {
      const updated = await taskService.changeTaskStatus(task.id, status);
      onUpdated(updated);
      setSaved(true);
    } catch (error) {
      setError(axios.isAxiosError(error) && typeof error.response?.data?.message === "string"
        ? error.response.data.message : "Unable to update status. Please try again.");
    } finally {
      pending.current = false;
      setSaving(false);
    }
  };
  return <Stack spacing={1} sx={{ mt: 2 }}>
    <TextField select label="Task status" value={task.status} disabled={saving}
      sx={{ maxWidth: 300 }} helperText={saving ? "Saving..." : "Available changes depend on your project role."}
      onChange={(event) => { void changeStatus(event.target.value as TaskStatus); }}>
      {[task.status, ...transitions[task.status]].map((status) =>
        <MenuItem key={status} value={status}>{status.replaceAll("_", " ")}</MenuItem>)}
    </TextField>
    {error && <Alert severity="error">{error}</Alert>}
    {saved && <Typography role="status" variant="body2" color="success.main">Status updated.</Typography>}
  </Stack>;
}
