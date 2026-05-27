import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import { useAuthStore } from '@/shared/store/authStore'
import { Bell } from 'lucide-react'

/**
 * Main application shell: sidebar + top header + scrollable content area.
 * The NotificationBell is a placeholder here — wired up properly in Phase 4.
 */
export default function DashboardLayout() {
  const { user } = useAuthStore()

  return (
    <div className="flex h-screen overflow-hidden bg-gray-50">
      <Sidebar />

      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top Header */}
        <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 flex-shrink-0">
          {/* Page title injected via React Router context in individual pages */}
          <div />

          <div className="flex items-center gap-4">
            {/* Notification bell — placeholder until Phase 4 */}
            <button className="relative p-2 text-gray-500 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors">
              <Bell size={20} />
            </button>

            {/* User avatar */}
            {user && (
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-full bg-brand/20 flex items-center justify-center">
                  <span className="text-brand text-sm font-semibold">
                    {user.firstName[0]}{user.lastName[0]}
                  </span>
                </div>
                <span className="text-sm font-medium text-gray-700 hidden md:block">
                  {user.firstName} {user.lastName}
                </span>
              </div>
            )}
          </div>
        </header>

        {/* Scrollable page content */}
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}