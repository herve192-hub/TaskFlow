import { Navigate } from "react-router-dom";
import { useCurrentUser } from '../features/auth/hooks/useCurrentUser'

interface Props {
    children: React.ReactNode;
}

/** Remove protected views as soon as tab metadata is cleared; the server remains the authentication boundary. */
export default function ProtectedRoute({
    children,
}: Props) {
    return useCurrentUser()
        ? children
        : <Navigate to="/login" replace/>;
}
