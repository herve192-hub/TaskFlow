import { createTheme} from "@mui/material/styles";

export const theme = createTheme({
    palette: {
        primary: {
            main: "#6366F1",
        },
        secondary: {
            main: "#8B5CF6",
        },
        background: {
            default: "#F8FAFC",
        },
    },

    typography: {
        fontFamily: "Inter, sans-serif",
    },

    shape: {
        borderRadius: 12,
    },
});