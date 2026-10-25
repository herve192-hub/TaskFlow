/**
 * Reusable react-hook-form password input for login and signup. Registration props
 * carry field validation while this component supplies a 128-character limit and
 * accessible visibility toggle. Toggle buttons do not submit the form or steal
 * input focus; password contents remain in the caller's transient form state.
 */

import { useState } from "react";
import { IconButton, InputAdornment, TextField } from "@mui/material";
import VisibilityIcon from "@mui/icons-material/Visibility";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import type { UseFormRegisterReturn } from "react-hook-form";

interface Props {
  label: string;
  autoComplete: "current-password" | "new-password";
  registration: UseFormRegisterReturn;
  disabled?: boolean;
  error?: boolean;
  helperText?: string;
}

/** Shared password visibility and accessible controls for sign-in and registration. */
export default function PasswordField({ registration, ...props }: Props) {
  const [visible, setVisible] = useState(false);
  return (
    <TextField
      {...props}
      {...registration}
      fullWidth
      margin="normal"
      type={visible ? "text" : "password"}
      slotProps={{
        htmlInput: { maxLength: 128 },
        input: {
          endAdornment: (
            <InputAdornment position="end">
              <IconButton
                type="button"
                edge="end"
                disabled={props.disabled}
                aria-label={(visible ? "Hide " : "Show ") + props.label.toLowerCase()}
                aria-pressed={visible}
                onMouseDown={event => event.preventDefault()}
                onClick={() => setVisible(value => !value)}
              >
                {visible ? <VisibilityOffIcon /> : <VisibilityIcon />}
              </IconButton>
            </InputAdornment>
          ),
        },
      }}
    />
  );
}
