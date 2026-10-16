import { Box } from "@mui/material";
import logo from "../assets/TaskFlow Gradient Flow Logo.png";

type TaskFlowLogoProps = {
  variant?: "horizontal" | "stacked" | "mark";
  width?: number;
};

// Frame the symbol and wordmark from the original artwork without changing it.
const symbolViewBox = "324 169 733 685";
const wordmarkViewBox = "77 898 1120 215";
const stackedViewBox = "77 169 1120 944";

function Artwork() {
  return <image href={logo} width="1254" height="1254" />;
}

export default function TaskFlowLogo({
  variant = "horizontal",
  width = variant === "stacked" ? 260 : variant === "mark" ? 40 : 180,
}: TaskFlowLogoProps) {
  const viewBox = variant === "horizontal"
    ? "0 0 240 52"
    : variant === "stacked" ? stackedViewBox : symbolViewBox;

  return (
    <Box
      component="svg"
      xmlns="http://www.w3.org/2000/svg"
      viewBox={viewBox}
      role="img"
      aria-label="TaskFlow"
      focusable="false"
      sx={{ display: "block", width, maxWidth: "100%", height: "auto", flexShrink: 0 }}
    >
      <title>TaskFlow</title>
      {variant === "horizontal" ? (
        <>
          <svg x="0" y="0" width="56" height="52" viewBox={symbolViewBox}>
            <Artwork />
          </svg>
          <svg x="68" y="10" width="172" height="33" viewBox={wordmarkViewBox}>
            <Artwork />
          </svg>
        </>
      ) : <Artwork />}
    </Box>
  );
}
