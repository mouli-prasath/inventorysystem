import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Gates a route behind authentication, and optionally behind a specific set of roles.
 * This mirrors the backend's RBAC rules for UX purposes (hiding/redirecting), but the
 * backend enforces the real security boundary regardless of what this component does -
 * see Section 10 of the backend review doc.
 */
export default function ProtectedRoute({ children, allowedRoles }) {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
}
