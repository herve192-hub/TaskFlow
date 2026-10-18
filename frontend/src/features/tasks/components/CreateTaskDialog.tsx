import { useRef, useState } from "react";
import type { FormEvent } from "react";
import { apiErrorMessage } from "../../../utils/apiError";
import {
  Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle,
  MenuItem, Stack, TextField,
} from "@mui/material";
import { TaskPriority, TaskType } from "../types/task.types";
import type { CreateTaskRequest } from "../types/task.types";
import WorkBadge from "../../../components/WorkBadge";

interface CreateTaskDialogProps {
  open: boolean;
  projectId: string;
  onClose: () => void;
  onCreate: (request: CreateTaskRequest) => Promise<void>;
}

/** Create within an explicit project, retaining the form and error message when the request fails. */
export default function CreateTaskDialog({ open, projectId, onClose, onCreate }: CreateTaskDialogProps) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState(TaskPriority.MEDIUM);
  const [type, setType] = useState(TaskType.TASK);
  const [dueDate, setDueDate] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const submitting = useRef(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (submitting.current || !title.trim()) return;
    // The synchronous guard prevents duplicate submissions before loading state reaches the UI.
    submitting.current = true;
    setLoading(true);
    setError(null);
    try {
      // datetime-local is interpreted in the browser's timezone and serialized as a UTC ISO timestamp.
      await onCreate({
        projectId, title: title.trim(), description: description.trim() || undefined,
        priority, type, dueDate: dueDate ? new Date(dueDate).toISOString() : undefined,
      });
      onClose();
    } catch (error) {
      setError(apiErrorMessage(error, "Unable to create task. Please try again."));
    } finally {
      submitting.current = false;
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={() => { if (!submitting.current) onClose(); }} fullWidth maxWidth="sm" aria-labelledby="create-task-title">
      <form onSubmit={handleSubmit}>
        <DialogTitle id="create-task-title">Create Task</DialogTitle>
        <DialogContent>
          <Stack spacing={3} sx={{ mt: 1 }}>
            {error && <Alert severity="error">{error}</Alert>}
            <TextField label="Task title" autoFocus required fullWidth value={title} disabled={loading}
              onChange={(event) => setTitle(event.target.value)} slotProps={{ htmlInput: { maxLength: 200 } }} />
            <TextField label="Description" multiline rows={4} fullWidth value={description} disabled={loading}
              onChange={(event) => setDescription(event.target.value)} slotProps={{ htmlInput: { maxLength: 5000 } }} />
            <TextField select label="Priority" value={priority} disabled={loading}
              onChange={(event) => setPriority(event.target.value as TaskPriority)}>
              {Object.values(TaskPriority).map((value) => <MenuItem key={value} value={value}><WorkBadge kind="priority" value={value} /></MenuItem>)}
            </TextField>
            <TextField select label="Type" value={type} disabled={loading}
              onChange={(event) => setType(event.target.value as TaskType)}>
              {Object.values(TaskType).map((value) => <MenuItem key={value} value={value}><WorkBadge kind="type" value={value} /></MenuItem>)}
            </TextField>
            <TextField label="Due date" helperText={`Enter time in your device's time zone (${Intl.DateTimeFormat().resolvedOptions().timeZone}).`} type="datetime-local" value={dueDate} disabled={loading}
              onChange={(event) => setDueDate(event.target.value)} slotProps={{ inputLabel: { shrink: true } }} />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button onClick={onClose} disabled={loading}>Cancel</Button>
          <Button type="submit" variant="contained" disabled={loading || !title.trim()}>
            {loading ? "Creating..." : "Create Task"}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
