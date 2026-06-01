import { createBrowserRouter, Navigate } from 'react-router-dom'
import { ROUTES } from './routes'
import RootLayout from '@/shared/components/layouts/RootLayout'
import AuthLayout from '@/shared/components/layouts/AuthLayout'
import DashboardLayout from '@/shared/components/layouts/DashboardLayout'
import ProtectedRoute from '@/shared/components/ProtectedRoute'
import RoleGuard from '@/shared/components/RoleGuard'
import LoginPage from '@/modules/auth/pages/LoginPage'
import RegisterPage from '@/modules/auth/pages/RegisterPage'
import CustomerDashboardPage from '@/modules/customer/pages/CustomerDashboardPage'
import SubmitTicketPage from '@/modules/ticket/pages/SubmitTicketPage'
import MyTicketsPage from '@/modules/ticket/pages/MyTicketsPage'
import TicketDetailPage from '@/modules/ticket/pages/TicketDetailPage'
import AgentDashboardPage from '@/modules/agent/pages/AgentDashboardPage'
import AgentTicketDetailPage from '@/modules/agent/pages/AgentTicketDetailPage'

// ── Placeholder page component used until real pages are built ─
function ComingSoon({ name }: { name: string }) {
  return (
    <div className="flex items-center justify-center h-64">
      <div className="text-center">
        <h2 className="text-xl font-semibold text-gray-700">{name}</h2>
        <p className="text-gray-400 mt-1 text-sm">Coming in the next phase</p>
      </div>
    </div>
  )
}

const router = createBrowserRouter([
  {
    // Root layout — provides QueryClient + Toaster
    path: '/',
    element: <RootLayout />,
    children: [

      // ── Redirect root to login ─────────────────────────────
      { index: true, element: <Navigate to={ROUTES.LOGIN} replace /> },

      // ── Public auth routes ─────────────────────────────────
      {
        element: <AuthLayout />,
        children: [
          { path: 'login',    element: <LoginPage /> },
          { path: 'register', element: <RegisterPage /> },
        ],
      },

      // ── Protected routes (require JWT) ─────────────────────
      {
        element: <ProtectedRoute />,
        children: [
          {
            element: <DashboardLayout />,
            children: [

              // Customer
              {
                element: <RoleGuard allowedRoles={['CUSTOMER']} redirectTo={ROUTES.LOGIN} />,
                children: [
                  { path: 'customer/dashboard',   element: <CustomerDashboardPage /> },
                  { path: 'customer/tickets',      element: <MyTicketsPage /> },
                  { path: 'customer/tickets/new',  element: <SubmitTicketPage /> },
                  { path: 'customer/tickets/:id',  element: <TicketDetailPage /> },
                ],
              },

              // Agent
              {
                element: <RoleGuard allowedRoles={['SUPPORT_AGENT']} redirectTo={ROUTES.LOGIN} />,
                children: [
                  { path: 'agent/dashboard',         element: <AgentDashboardPage /> },
                  { path: 'agent/tickets/:id',       element: <AgentTicketDetailPage /> },
                ],
              },

              // Supervisor
              {
                element: <RoleGuard allowedRoles={['SUPERVISOR', 'ADMIN']} redirectTo={ROUTES.LOGIN} />,
                children: [
                  { path: 'supervisor/dashboard',    element: <ComingSoon name="Supervisor Dashboard" /> },
                  { path: 'supervisor/escalations',  element: <ComingSoon name="Escalations" /> },
                  { path: 'supervisor/sla',          element: <ComingSoon name="SLA Compliance" /> },
                ],
              },

              // Admin
              {
                element: <RoleGuard allowedRoles={['ADMIN']} redirectTo={ROUTES.LOGIN} />,
                children: [
                  { path: 'admin/dashboard',         element: <ComingSoon name="Admin Dashboard" /> },
                  { path: 'admin/users',             element: <ComingSoon name="User Management" /> },
                  { path: 'admin/sla',               element: <ComingSoon name="SLA Policies" /> },
                  { path: 'admin/analytics',         element: <ComingSoon name="Analytics" /> },
                ],
              },

            ],
          },
        ],
      },

      // 404 fallback
      { path: '*', element: <Navigate to={ROUTES.LOGIN} replace /> },
    ],
  },
])

export default router