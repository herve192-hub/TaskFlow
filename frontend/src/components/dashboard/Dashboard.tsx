import { Link as RouterLink } from "react-router-dom";
import { Alert, Box, Button, CircularProgress, Grid, LinearProgress, Paper, Stack, Typography } from "@mui/material";
import TaskAltIcon from "@mui/icons-material/TaskAlt";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import FolderOpenIcon from "@mui/icons-material/FolderOpen";
import StatCard from "./StatCard";
import { useWorkspace } from "../../features/tasks/hooks/useWorkspace";
import { TaskStatus } from "../../features/tasks/types/task.types";

export default function Dashboard() {
  const { data, loading, error, refresh } = useWorkspace();
  const tasks = data?.tasks ?? [];
  const completed = tasks.filter((task) => task.status === TaskStatus.COMPLETED).length;
  const progress = tasks.length ? Math.round(completed / tasks.length * 100) : 0;
  const deadlines = tasks.filter((task) => task.dueDate && task.status !== TaskStatus.COMPLETED && task.status !== TaskStatus.CANCELLED)
    .sort((a, b) => Date.parse(a.dueDate!) - Date.parse(b.dueDate!)).slice(0, 5);
  return <Box sx={{ p: { xs: 0, md: 4 } }}>
    <Stack direction="row" sx={{ justifyContent: "space-between", mb: 3 }}>
      <Box><Typography variant="h4" sx={{ fontWeight: 700 }}>Welcome Back</Typography>
        <Typography color="text.secondary">An overview of your projects and their unarchived tasks.</Typography></Box>
      <Button onClick={refresh} disabled={loading}>Refresh</Button>
    </Stack>
    <Stack direction="row" spacing={2} sx={{ mb: 3 }}>
      <Button component={RouterLink} to="/dashboard/projects" variant="contained">View Projects</Button>
      <Button component={RouterLink} to="/dashboard/tasks" variant="outlined">View Tasks</Button>
    </Stack>
    {loading ? <Box sx={{ textAlign: "center", py: 8 }}><CircularProgress aria-label="Loading dashboard" /></Box>
      : error ? <Alert severity="error" action={<Button color="inherit" onClick={refresh}>Retry</Button>}>{error}</Alert>
      : <Stack spacing={3}>
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 4 }}><StatCard title="Total Tasks" value={tasks.length} icon={<TaskAltIcon color="primary" />} /></Grid>
          <Grid size={{ xs: 12, md: 4 }}><StatCard title="Completed" value={completed} icon={<CheckCircleIcon color="primary" />} /></Grid>
          <Grid size={{ xs: 12, md: 4 }}><StatCard title="Projects" value={data?.projects.length ?? 0} icon={<FolderOpenIcon color="primary" />} /></Grid>
        </Grid>
        <Paper variant="outlined" sx={{ p: 3, borderRadius: 3 }}>
          <Typography variant="h6">Task completion</Typography>
          <Typography color="text.secondary" sx={{ mb: 2 }}>{completed} of {tasks.length} tasks completed ({progress}%)</Typography>
          <LinearProgress variant="determinate" value={progress} aria-label="Task completion" />
        </Paper>
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 6 }}><Paper variant="outlined" sx={{ p: 3, borderRadius: 3 }}>
            <Typography variant="h6" sx={{ mb: 2 }}>Recently created tasks</Typography>
            {!tasks.length && <Typography color="text.secondary">No tasks yet. Open a project to create one.</Typography>}
            {tasks.slice(0, 5).map((task) => <Box key={task.id} sx={{ mb: 2 }}>
              <Button component={RouterLink} to={`/dashboard/projects/${encodeURIComponent(task.projectId)}/tasks`} sx={{ textAlign: "left", overflowWrap: "anywhere" }}>{task.title}</Button>
              <Typography variant="body2" color="text.secondary">{task.status.replaceAll("_", " ")}</Typography>
            </Box>)}
          </Paper></Grid>
          <Grid size={{ xs: 12, md: 6 }}><Paper variant="outlined" sx={{ p: 3, borderRadius: 3 }}>
            <Typography variant="h6" sx={{ mb: 2 }}>Open deadlines</Typography>
            {!deadlines.length && <Typography color="text.secondary">No open tasks with deadlines.</Typography>}
            {deadlines.map((task) => <Box key={task.id} sx={{ mb: 2 }}>
              <Button component={RouterLink} to={`/dashboard/projects/${encodeURIComponent(task.projectId)}/tasks`} sx={{ textAlign: "left", overflowWrap: "anywhere" }}>{task.title}</Button>
              <Typography variant="body2" color="text.secondary">Due {new Date(task.dueDate!).toLocaleString()}</Typography>
            </Box>)}
          </Paper></Grid>
        </Grid>
      </Stack>}
  </Box>;
}
