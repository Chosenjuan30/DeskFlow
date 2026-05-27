import { NavLink, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/shared/store/authStore'
import { ROUTES } from '@/router/routes'
import { cn } from '@/lib/utils'
import {
  LayoutDashboard,
  Ticket,
  PlusCircle,
  Users,
  BarChart3,
  Settings,
  AlertTriangle,
  ClipboardList,
  LogOut,
} from 'lucide-react'
import type { UserRole } from '@/shared/types/common.types'

interface NavItem {
  label: string
  path: string
  icon: React.ReactNode
}

const navByRole: Record<UserRole, NavItem[]> = {
  CUSTOMER: [
    { label: 'Dashboard',    path: ROUTES.CUSTOMER_DASHBOARD,  icon: <LayoutDashboard size={18} /> },
    { label: 'My Tickets',   path: ROUTES.CUSTOMER_TICKETS,    icon: <Ticket size={18} /> },
    { label: 'New Ticket',   path: ROUTES.CUSTOMER_TICKET_NEW, icon: <PlusCircle size={18} /> },
  ],
  SUPPORT_AGENT: [
    { label: 'My Queue',     path: ROUTES.AGENT_DASHBOARD,     icon: <ClipboardList size={18} /> },
  ],
  SUPERVISOR: [
    { label: 'Dashboard',    path: ROUTES.SUPERVISOR_DASHBOARD,   icon: <LayoutDashboard size={18} /> },
    { label: 'Escalations',  path: ROUTES.SUPERVISOR_ESCALATIONS, icon: <AlertTriangle size={18} /> },
    { label: 'SLA Reports',  path: ROUTES.SUPERVISOR_SLA,         icon: <BarChart3 size={18} /> },
  ],
  ADMIN: [
    { label: 'Dashboard',    path: ROUTES.ADMIN_DASHBOARD, icon: <LayoutDashboard size={18} /> },
    { label: 'Users',        path: ROUTES.ADMIN_USERS,     icon: <Users size={18} /> },
    { label: 'SLA Policies', path: ROUTES.ADMIN_SLA,       icon: <Settings size={18} /> },
    { label: 'Analytics',    path: ROUTES.ADMIN_ANALYTICS, icon: <BarChart3 size={18} /> },
  ],
}

export default function Sidebar() {
  const { user, clearAuth } = useAuthStore()
  const navigate = useNavigate()
  const items = user ? navByRole[user.role] : []

  const handleLogout = () => {
    clearAuth()
    navigate(ROUTES.LOGIN)
  }

  return (
    <aside className="w-60 h-full bg-white border-r border-gray-200 flex flex-col">
      {/* Brand */}
      <div className="h-16 flex items-center px-6 border-b border-gray-200">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-md bg-brand flex items-center justify-center">
            <span className="text-white font-bold text-xs">D</span>
          </div>
          <span className="font-semibold text-gray-900">DeskFlow</span>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        {items.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors',
                isActive
                  ? 'bg-brand/10 text-brand'
                  : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
              )
            }
          >
            {item.icon}
            {item.label}
          </NavLink>
        ))}
      </nav>

      {/* User info + logout */}
      <div className="px-3 py-4 border-t border-gray-200">
        {user && (
          <div className="mb-2 px-3">
            <p className="text-xs font-medium text-gray-900 truncate">
              {user.firstName} {user.lastName}
            </p>
            <p className="text-xs text-gray-500 truncate">{user.email}</p>
          </div>
        )}
        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium text-gray-600 hover:bg-red-50 hover:text-red-600 transition-colors"
        >
          <LogOut size={18} />
          Sign out
        </button>
      </div>
    </aside>
  )
}