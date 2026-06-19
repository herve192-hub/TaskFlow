// 
import Button from "@mui/material/Button";
import { SxProps } from "@mui/material/styles";

interface oAuthButtonProps {
  // provider: "Google" | "GitHub";
  text: string;
  icon?: React.ReactNode;
  onClick?: () => void;
  sx?: SxProps;
}

export default function OAuthButton({ 
  text, 
  icon, 
  onClick,
  sx,
}: oAuthButtonProps) {
  return (
    <Button
      fullWidth
      variant = "outlined" 
      size = "large"
      onClick = {onClick}
      startIcon = {icon}
      sx = {{
        py: 1.5,
        textTransform: "none",
        fontWeight: "600",
        ...sx,
      }}
    >
      {text}
    </Button>
  );
}