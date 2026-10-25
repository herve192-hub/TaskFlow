/**
 * Controlled project creation form owned by the Projects page. Trims the request,
 * collects visibility metadata, and leaves the backend to set owner/membership.
 * A pending ref prevents duplicate submissions before React disables controls.
 * Close/reset follows successful creation; rejected writes retain inputs for retry.
 */

import { useRef, useState } from "react";
import type { FormEvent } from "react";
import { apiErrorMessage } from "../../../utils/apiError";

import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
} from "@mui/material";

import type {
  CreateProjectRequest,
} from "../types/project.types";
import { ProjectVisibility } from "../types/project.types";

interface CreateProjectDialogProps {
  open: boolean;
  onClose: () => void;
  onCreate: (request: CreateProjectRequest) => Promise<void>;
}

/** Retain entered values on failure; the parent unmounts the dialog after success or cancellation. */
export default function CreateProjectDialog({
  open,
  onClose,
  onCreate,
}: CreateProjectDialogProps) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [visibility, setVisibility] =
    useState<ProjectVisibility>(ProjectVisibility.PRIVATE);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const submitting = useRef(false);

  const handleClose = () => {
    if (submitting.current) {
      return;
    }

    onClose();
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const trimmedName = name.trim();

    if (submitting.current || trimmedName.length < 2) {
      return;
    }

    submitting.current = true;
    setLoading(true);
    setError(null);
    try {
      await onCreate({
        name: trimmedName,
        description: description.trim() || undefined,
        visibility,
      });
      onClose();
    } catch (error) {
      setError(apiErrorMessage(error, "Unable to create project. Please try again."));
    } finally {
      submitting.current = false;
      setLoading(false);
    }
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      fullWidth
      maxWidth="sm"
      aria-labelledby="create-project-title"
    >
      <form onSubmit={handleSubmit}>
        <DialogTitle id="create-project-title">Create Project</DialogTitle>

        <DialogContent>
          <Stack spacing={3} sx={{ mt: 1 }}>
            {error && <Alert severity="error">{error}</Alert>}
            <TextField
              label="Project name"
              value={name}
              onChange={(event) => setName(event.target.value)}
              required
              disabled={loading}
              fullWidth
              slotProps={{
                htmlInput: { maxLength: 100 },
              }}
            />

            <TextField
              label="Description"
              value={description}
              onChange={(event) =>
                setDescription(event.target.value)
              }
              multiline
              disabled={loading}
              rows={4}
              fullWidth
              slotProps={{
                htmlInput: { maxLength: 500 },
              }}
            />

            <FormControl fullWidth disabled={loading}>
              <InputLabel id="project-visibility-label">
                Visibility
              </InputLabel>

              <Select<ProjectVisibility>
                labelId="project-visibility-label"
                label="Visibility"
                value={visibility}
                onChange={(event) =>
                  setVisibility(
                    event.target.value
                  )
                }
              >
                <MenuItem value={ProjectVisibility.PRIVATE}>Private</MenuItem>
                <MenuItem value={ProjectVisibility.TEAM}>Team</MenuItem>
                <MenuItem value={ProjectVisibility.ORGANIZATION}>
                  Organization
                </MenuItem>
                <MenuItem value={ProjectVisibility.PUBLIC}>Public</MenuItem>
              </Select>
            </FormControl>
          </Stack>
        </DialogContent>

        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button
            onClick={handleClose}
            disabled={loading}
          >
            Cancel
          </Button>

          <Button
            variant="contained"
            type="submit"
            disabled={loading || name.trim().length < 2}
          >
            {loading ? "Creating..." : "Create Project"}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
