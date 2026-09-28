import { useState } from "react";
import { Link as RouterLink } from "react-router-dom";
import { Alert, Box, Button, Card, CardContent, Chip, CircularProgress, MenuItem, Pagination, Stack, TextField, Typography } from "@mui/material";
import { useWorkspace } from "../features/tasks/hooks/useWorkspace";
import CreateTaskDialog from "../features/tasks/components/CreateTaskDialog";
import { taskService } from "../features/tasks/services/taskService";

export default function Tasks() {
  const { data, loading, error, refresh } = useWorkspace();
  const [projectId, setProjectId] = useState("");
  const [page, setPage] = useState(1);
  const [creating, setCreating] = useState(false);
  const tasks = data?.tasks.filter((task) => !projectId || task.projectId === projectId) ?? [];
  const pageCount = Math.ceil(tasks.length / 20);
  const currentPage = Math.min(page, Math.max(1, pageCount));
  const names = new Map(data?.projects.map((project) => [project.id, project.name]));
  return <Box>
    <Stack direction={{ xs: "column", sm: "row" }} spacing={2} sx={{ mb: 3, justifyContent: "space-between" }}>
      <Box><Typography variant="h4" sx={{ fontWeight: 700 }}>Tasks</Typography>
        <Typography color="text.secondary">Tasks across your projects. Select a project to create a task.</Typography></Box>
      <Button onClick={refresh} disabled={loading}>Refresh</Button>
    </Stack>
    {loading ? <Box sx={{ textAlign: "center", py: 8 }}><CircularProgress aria-label="Loading tasks" /></Box>
      : error ? <Alert severity="error" action={<Button color="inherit" onClick={refresh}>Retry</Button>}>{error}</Alert>
      : <Stack spacing={2}>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
          <TextField select label="Project" value={projectId} sx={{ minWidth: 240 }} onChange={(event) => { setProjectId(event.target.value); setPage(1); }}>
            <MenuItem value="">All my projects</MenuItem>
            {data?.projects.map((project) => <MenuItem key={project.id} value={project.id}>{project.name}</MenuItem>)}
          </TextField>
          <Button variant="contained" disabled={!projectId} onClick={() => setCreating(true)}>Create Task</Button>
        </Stack>
        <Typography color="text.secondary">{tasks.length} task{tasks.length === 1 ? "" : "s"}</Typography>
        {!tasks.length && <Box sx={{ py: 6, textAlign: "center" }}><Typography variant="h6">No tasks yet</Typography>
          <Button component={RouterLink} to="/dashboard/projects">View projects</Button></Box>}
        {tasks.slice((currentPage - 1) * 20, currentPage * 20).map((task) => <Card key={task.id} variant="outlined"><CardContent>
          <Typography variant="h6" sx={{ overflowWrap: "anywhere" }}>{task.title}</Typography>
          <Button component={RouterLink} to={`/dashboard/projects/${encodeURIComponent(task.projectId)}/tasks`}>{names.get(task.projectId) ?? "View project"}</Button>
          <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: "wrap" }}>
            <Chip size="small" label={task.status.replaceAll("_", " ")} />
            <Chip size="small" variant="outlined" label={task.priority} />
            <Chip size="small" variant="outlined" label={task.type} />
          </Stack>
          {task.dueDate && <Typography variant="body2" sx={{ mt: 2 }}>Due {new Date(task.dueDate).toLocaleString()}</Typography>}
        </CardContent></Card>)}
        {pageCount > 1 && <Pagination count={pageCount} page={currentPage} onChange={(_, value) => setPage(value)} />}
      </Stack>}
    {creating && projectId && <CreateTaskDialog open projectId={projectId} onClose={() => setCreating(false)} onCreate={async (request) => { await taskService.createTask(request); refresh(); }} />}
  </Box>;
}
