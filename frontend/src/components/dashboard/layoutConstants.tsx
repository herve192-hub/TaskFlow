/**
 * Shared full/compact drawer widths in pixels. Sidebar geometry and Topbar offsets
 * must change together to avoid overlap; keep both consumers tied to these values.
 * DashboardLayout selects the compact state for small screens.
 */

// Shared drawer widths keep navigation layout and fixed-header offsets synchronized.
export const drawerWidth = 240;
export const collapsedDrawerWidth = 72;
