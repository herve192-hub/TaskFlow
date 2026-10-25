/**
 * Client navigation guard subscribed to the active account through useCurrentUser.
 * Missing display metadata redirects to login; this is a UI convenience, not proof
 * of authorization. Axios sends the HttpOnly cookie and handles rejected sessions.
 */

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
