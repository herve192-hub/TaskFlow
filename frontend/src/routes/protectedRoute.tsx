import { Navigate } from "react-router-dom";
import { isAuthenticated } from '../features/auth/session'

interface Props {
    children: React.ReactNode;
}

export default function ProtectedRoute({
    children,
}: Props) {
    return isAuthenticated()
        ? children
        : <Navigate to="/" replace/>;
}
