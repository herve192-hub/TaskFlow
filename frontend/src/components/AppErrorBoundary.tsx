/**
 * Last-resort React render boundary enclosing routing, including lazy chunk errors.
 * Shows a safe reload action without printing exception details into the UI.
 * This handles render failures; asynchronous API errors belong in page-level state
 * and event handlers. A full reload obtains current deployment assets.
 */

import { Component } from "react";
import type { ReactNode } from "react";
import { Alert, Box, Button, Typography } from "@mui/material";

/** Recover from render failures and stale route chunks without exposing internal error details. */
export default class AppErrorBoundary extends Component<{ children: ReactNode }, { failed: boolean }> {
  state = { failed: false };

  static getDerivedStateFromError() {
    return { failed: true };
  }

  render() {
    if (!this.state.failed) return this.props.children;

    return (
      <Box component="main" sx={{ maxWidth: 560, mx: "auto", p: 3, mt: 8 }}>
        <Typography variant="h4" component="h1" sx={{ mb: 2 }}>Unable to display TaskFlow</Typography>
        <Alert severity="error" sx={{ mb: 3 }}>
          Reload the application to try again. If the problem continues, contact your administrator.
        </Alert>
        <Button variant="contained" onClick={() => window.location.reload()}>Reload application</Button>
      </Box>
    );
  }
}
