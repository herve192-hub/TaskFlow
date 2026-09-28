import { useRef, useState } from "react";
import type { FormEvent } from "react";
import axios from "axios";
import {
  Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle,
  MenuItem, Stack, TextField,
} from "@mui/material";
import { TaskPriority, TaskType } from "../types/task.types";
import type { CreateTaskRequest } from "../types/task.types";

interface CreateTaskDialogProps {
  open: boolean;
  projectId: string;
  onClose: () => void;
  onCreate: (request: CreateTaskRequest) => Promise<void>;
}

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
    submitting.current = true;
    setLoading(true);
    setError(null);
    try {
      await onCreate({
        projectId, title: title.trim(), description: description.trim() || undefined,
        priority, type, dueDate: dueDate ? new Date(dueDate).toISOString() : undefined,
      });
      onClose();
    } catch (error) {
      setError(axios.isAxiosError(error) && typeof error.response?.data?.message === "string"
        ? error.response.data.message : "Unable to create task. Please try again.");
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
              {Object.values(TaskPriority).map((value) => <MenuItem key={value} value={value}>{value}</MenuItem>)}
            </TextField>
            <TextField select label="Type" value={type} disabled={loading}
              onChange={(event) => setType(event.target.value as TaskType)}>
              {Object.values(TaskType).map((value) => <MenuItem key={value} value={value}>{value}</MenuItem>)}
            </TextField>
            <TextField label="Due date" type="datetime-local" value={dueDate} disabled={loading}
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
