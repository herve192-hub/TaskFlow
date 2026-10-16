import AppRoutes from "./routes/AppRoutes.tsx";

/** Root route composition; application-wide providers are mounted once in main.tsx. */
function App() {
    return <AppRoutes />;
}

export default App;
