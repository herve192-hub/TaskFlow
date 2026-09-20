import { useState } from "react";

import {
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
  loading?: boolean;
  onClose: () => void;
  onCreate: (request: CreateProjectRequest) => Promise<void>;
}

export default function CreateProjectDialog({
  open,
  loading = false,
  onClose,
  onCreate,
}: CreateProjectDialogProps) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [visibility, setVisibility] =
    useState<ProjectVisibility>(ProjectVisibility.PRIVATE);

  const reset = () => {
    setName("");
    setDescription("");
    setVisibility(ProjectVisibility.PRIVATE);
  };

  const handleClose = () => {
    if (loading) {
      return;
    }

    reset();
    onClose();
  };

  const handleSubmit = async () => {
    const trimmedName = name.trim();

    if (trimmedName.length < 2) {
      return;
    }

    await onCreate({
      name: trimmedName,
      description: description.trim() || undefined,
      visibility,
    });

    reset();
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      fullWidth
      maxWidth="sm"
    >
      <DialogTitle>Create Project</DialogTitle>

      <DialogContent>
        <Stack spacing={3} sx={{ mt: 1 }}>
          <TextField
            label="Project name"
            value={name}
            onChange={(event) => setName(event.target.value)}
            required
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
            rows={4}
            fullWidth
            slotProps={{
              htmlInput: { maxLength: 500 },
            }}
          />

          <FormControl fullWidth>
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
          onClick={handleSubmit}
          disabled={loading || name.trim().length < 2}
        >
          {loading ? "Creating..." : "Create Project"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
