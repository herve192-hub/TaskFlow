import AppRoutes from "./routes/AppRoutes.tsx";
import AppErrorBoundary from "./components/AppErrorBoundary";

/** Root route composition; application-wide providers are mounted once in main.tsx. */
function App() {
    return <AppErrorBoundary><AppRoutes /></AppErrorBoundary>;
}

export default App;
