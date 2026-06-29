import {
  Paper,
  Typography,
  Stack,
  Chip,
} from "@mui/material";

import WbSunnyOutlinedIcon from "@mui/icons-material/WbSunnyOutlined";

export default function DashboardSummary() {
  return (
    <Paper
      elevation={3}
      sx={{
        p: 3,
        borderRadius: 3,
        border: "1px solid",
        borderColor: "divider",
        width: { xs: "100%", md: "50%" },
        minWidth: { xs: 0, md: 320 },
      }}
    >
      <Stack spacing={2}>
        <Typography variant="h5" sx={{ fontWeight: 700}}>
          Good Morning 👋
        </Typography>

        <Typography color="text.secondary">
          Ready to get things done today?
        </Typography>

        <Stack direction="row" sx={{ spacing: 1, flexWrap: "wrap", gap: 2, }}>
          <Chip
            icon={<WbSunnyOutlinedIcon />}
            label="8 Tasks Today"
            color="primary"
          />

          <Chip
            label="3 Deadlines"
            color="warning"
          />

          <Chip
            label="91% Productivity"
            color="success"
          />
        </Stack>
      </Stack>
    </Paper>
  );
}