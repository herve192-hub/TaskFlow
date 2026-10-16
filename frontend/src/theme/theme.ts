import { alpha, createTheme } from "@mui/material/styles";

// The logo's blue/cyan/teal flow anchors the UI; darker shades keep text readable on white.
export const brand = {
    navy: "#06213B",
    blue: "#0066CC",
    blueDark: "#004EAA",
    cyan: "#007FA3",
    teal: "#007F78",
    tealDark: "#00645F",
    blueTint: "#E8F3FF",
    cyanTint: "#E5F6FB",
    tealTint: "#E2F5F1",
    canvas: "#F4F9FC",
    border: "#D5E4EE",
    muted: "#516B7C",
} as const;

export const brandGradient = `linear-gradient(115deg, ${brand.blue} 0%, ${brand.teal} 100%)`;
export const brandWash = `linear-gradient(135deg, ${brand.blueTint} 0%, ${brand.cyanTint} 55%, ${brand.tealTint} 100%)`;

/** Application-wide design tokens consumed by the single ThemeProvider in main.tsx. */
export const theme = createTheme({
    palette: {
        primary: {
            main: brand.blue,
            light: brand.blueTint,
            dark: brand.blueDark,
            contrastText: "#FFFFFF",
        },
        secondary: {
            main: brand.teal,
            light: brand.tealTint,
            dark: brand.tealDark,
            contrastText: "#FFFFFF",
        },
        info: {
            main: brand.cyan,
            light: brand.cyanTint,
            dark: "#006580",
            contrastText: "#FFFFFF",
        },
        success: {
            main: brand.teal,
            light: brand.tealTint,
            dark: brand.tealDark,
            contrastText: "#FFFFFF",
        },
        background: {
            default: brand.canvas,
            paper: "#FFFFFF",
        },
        text: {
            primary: brand.navy,
            secondary: brand.muted,
        },
        divider: brand.border,
        action: {
            active: brand.muted,
            hover: alpha(brand.blue, 0.05),
            selected: alpha(brand.blue, 0.09),
            focus: alpha(brand.blue, 0.14),
        },
    },

    typography: {
        fontFamily: "Inter, sans-serif",
    },

    shape: {
        borderRadius: 10,
    },

    components: {
        MuiCssBaseline: {
            styleOverrides: {
                a: { color: brand.blue, textUnderlineOffset: "3px" },
                "::selection": { backgroundColor: brand.tealTint, color: brand.navy },
                ":root": {
                    "--toastify-color-info": brand.cyan,
                    "--toastify-color-success": brand.teal,
                    "--toastify-text-color-light": brand.navy,
                },
            },
        },
        MuiButton: {
            defaultProps: { disableElevation: true },
            styleOverrides: {
                root: {
                    textTransform: "none",
                    fontWeight: 600,
                    "&.Mui-focusVisible": { outline: `2px solid ${brand.blue}`, outlineOffset: 3 },
                    "&.MuiButton-contained.MuiButton-colorPrimary": {
                        backgroundImage: brandGradient,
                        "&:hover": {
                            backgroundImage: `linear-gradient(115deg, ${brand.blueDark}, ${brand.tealDark})`,
                        },
                        // Disabled actions retain MUI's distinct disabled colors.
                        "&.Mui-disabled": { backgroundImage: "none" },
                    },
                },
            },
        },
        MuiOutlinedInput: {
            styleOverrides: {
                root: {
                    backgroundColor: "#FFFFFF",
                    "&:hover .MuiOutlinedInput-notchedOutline": { borderColor: brand.muted },
                },
                notchedOutline: { borderColor: "#7893A5" },
            },
        },
        MuiListItemButton: {
            styleOverrides: {
                root: {
                    "&.Mui-selected": {
                        color: brand.blueDark,
                        backgroundColor: brand.blueTint,
                        "& .MuiListItemIcon-root": { color: brand.blue },
                    },
                    "&.Mui-selected:hover": { backgroundColor: alpha(brand.blue, 0.13) },
                },
            },
        },
        MuiChip: {
            styleOverrides: {
                colorDefault: {
                    backgroundColor: brand.blueTint,
                    color: brand.navy,
                    "&.MuiChip-outlined": { borderColor: brand.border, backgroundColor: "transparent" },
                },
            },
        },
        MuiAvatar: {
            styleOverrides: {
                colorDefault: { backgroundColor: brand.blueTint, color: brand.blueDark },
            },
        },
        MuiLinearProgress: {
            styleOverrides: {
                root: { height: 6, borderRadius: 3 },
                colorPrimary: {
                    backgroundColor: brand.blueTint,
                    "& .MuiLinearProgress-bar": { backgroundImage: brandGradient },
                },
                bar: { borderRadius: 3 },
            },
        },
        MuiLink: {
            styleOverrides: { root: { fontWeight: 500 } },
        },
    },
});
