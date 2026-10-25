/**
 * Application boundary around all routes. AppErrorBoundary turns render/lazy-chunk
 * failures into a reload action; individual pages handle expected API errors locally.
 * Global providers belong in main.tsx so route changes do not recreate them.
 */

import AppRoutes from "./routes/AppRoutes.tsx";
import AppErrorBoundary from "./components/AppErrorBoundary";

/** Root route composition; application-wide providers are mounted once in main.tsx. */
function App() {
    return <AppErrorBoundary><AppRoutes /></AppErrorBoundary>;
}

export default App;
