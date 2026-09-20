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
  ProjectVisibility,
} from "../types/project.types";

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
    useState<ProjectVisibility>("PRIVATE");

  const reset = () => {
    setName("");
    setDescription("");
    setVisibility("PRIVATE");
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
            inputProps={{
              maxLength: 100,
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
            inputProps={{
              maxLength: 500,
            }}
          />

          <FormControl fullWidth>
            <InputLabel id="project-visibility-label">
              Visibility
            </InputLabel>

            <Select
              labelId="project-visibility-label"
              label="Visibility"
              value={visibility}
              onChange={(event) =>
                setVisibility(
                  event.target.value as ProjectVisibility
                )
              }
            >
              <MenuItem value="PRIVATE">Private</MenuItem>
              <MenuItem value="TEAM">Team</MenuItem>
              <MenuItem value="ORGANIZATION">
                Organization
              </MenuItem>
              <MenuItem value="PUBLIC">Public</MenuItem>
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