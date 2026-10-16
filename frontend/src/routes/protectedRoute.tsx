import { Navigate } from "react-router-dom";
import { isAuthenticated } from '../features/auth/session'

interface Props {
    children: React.ReactNode;
}

/** Navigation convenience based on tab metadata; server-side cookie validation is the security boundary. */
export default function ProtectedRoute({
    children,
}: Props) {
    return isAuthenticated()
        ? children
        : <Navigate to="/" replace/>;
}
