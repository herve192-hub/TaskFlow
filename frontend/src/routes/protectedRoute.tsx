import { Navigate } from "react-router-dom";

interface Props {
    children: React.ReactNode;
}

export default function ProtectedRoute({
    children,
}: Props) {
    const token = localStorage.getItem(
        "accessToken"
    );

    return token
        ? children
        : <Navigate to="/" replace/>;
}