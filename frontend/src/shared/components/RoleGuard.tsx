import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/shared/store/authStore'
import type { UserRole } from '@/shared/types/common.types'

interface Props {
  allowedRoles: UserRole[]
  /** Where to send users who don't have the required role */
  redirectTo?: string
}

/**
 * Restricts a group of routes to specific roles.
 * Place inside a ProtectedRoute so unauthenticated users
 * are caught before role checking.
 */
export default function RoleGuard({ allowedRoles, redirectTo = '/' }: Props) {
  const { user } = useAuthStore()

  if (!user || !allowedRoles.includes(user.role)) {
    return <Navigate to={redirectTo} replace />
  }

  return <Outlet />
}