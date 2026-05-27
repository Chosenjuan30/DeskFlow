import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/shared/store/authStore'
import { ROUTES } from '@/router/routes'

/**
 * Wraps all authenticated routes.
 * Redirects to /login if no access token exists, preserving
 * the intended destination so the user lands there after login.
 */
export default function ProtectedRoute() {
  const { accessToken } = useAuthStore()
  const location = useLocation()

  if (!accessToken) {
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />
  }

  return <Outlet />
}