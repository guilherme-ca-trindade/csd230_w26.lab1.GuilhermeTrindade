import { Navigate, Outlet } from 'react-router';
import useAuth from './useAuth.js';

// A browser-side redirect only. It does not validate the token - Spring still checks
// every protected request.
export default function ProtectedRoute() {
  const { token } = useAuth();
  return token ? <Outlet /> : <Navigate to="/login" replace />;
}
