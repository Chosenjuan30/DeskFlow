import { Link } from 'react-router-dom'
import { format } from 'date-fns'
import { PlusCircle, Ticket as TicketIcon, AlertCircle } from 'lucide-react'
import { useMyTickets } from '@/modules/ticket/hooks/useTickets'
import StatusBadge from '@/modules/ticket/components/StatusBadge'
import PriorityBadge from '@/modules/ticket/components/PriorityBadge'
import { ROUTES, buildPath } from '@/router/routes'
import { useAuthStore } from '@/shared/store/authStore'

export default function CustomerDashboardPage() {
  const { user } = useAuthStore()

  const { data: openData }   = useMyTickets({ status: 'OPEN', size: 1 })
  const { data: recentData } = useMyTickets({ size: 5 })

  return (
    <div>
      <h1 className="text-xl font-semibold text-gray-900 mb-1">
        Welcome back, {user?.firstName}
      </h1>
      <p className="text-sm text-gray-500 mb-6">Here's an overview of your support tickets.</p>

      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
        <StatCard
          icon={<AlertCircle size={20} className="text-amber-500" />}
          label="Open tickets"
          value={openData?.totalElements ?? '—'}
          bg="bg-amber-50"
        />
        <StatCard
          icon={<TicketIcon size={20} className="text-brand" />}
          label="Total tickets"
          value={recentData?.totalElements ?? '—'}
          bg="bg-brand-light"
        />
        <div className="flex flex-col items-start justify-between bg-white rounded-xl border border-gray-200 p-4">
          <p className="text-sm text-gray-500 mb-3">Need help?</p>
          <Link
            to={ROUTES.CUSTOMER_TICKET_NEW}
            className="flex items-center gap-2 px-3 py-2 bg-brand text-white text-sm font-medium rounded-lg hover:bg-brand-dark transition-colors w-full justify-center"
          >
            <PlusCircle size={16} />
            New ticket
          </Link>
        </div>
      </div>

      {/* Recent tickets */}
      <div className="bg-white rounded-xl border border-gray-200">
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
          <h2 className="text-sm font-semibold text-gray-900">Recent tickets</h2>
          <Link to={ROUTES.CUSTOMER_TICKETS} className="text-xs text-brand hover:underline">
            View all
          </Link>
        </div>

        {recentData?.content.length === 0 ? (
          <div className="py-10 text-center text-gray-400 text-sm">
            <p>No tickets yet.</p>
            <Link to={ROUTES.CUSTOMER_TICKET_NEW} className="text-brand hover:underline mt-1 inline-block">
              Submit your first ticket
            </Link>
          </div>
        ) : (
          <ul className="divide-y divide-gray-100">
            {recentData?.content.map((ticket) => (
              <li key={ticket.id} className="px-5 py-3 hover:bg-gray-50 transition-colors">
                <Link to={buildPath(ROUTES.CUSTOMER_TICKET, { id: ticket.id })} className="flex items-center gap-3">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-0.5">
                      <span className="font-mono text-xs text-gray-400">{ticket.referenceNumber}</span>
                      <StatusBadge status={ticket.status} />
                      <PriorityBadge priority={ticket.priority} />
                    </div>
                    <p className="text-sm font-medium text-gray-900 truncate">{ticket.title}</p>
                  </div>
                  <span className="text-xs text-gray-400 flex-shrink-0">
                    {format(new Date(ticket.createdAt), 'MMM d')}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}

function StatCard({ icon, label, value, bg }: {
  icon: React.ReactNode
  label: string
  value: number | string
  bg: string
}) {
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-4">
      <div className={`w-9 h-9 rounded-lg ${bg} flex items-center justify-center mb-3`}>{icon}</div>
      <p className="text-2xl font-bold text-gray-900">{value}</p>
      <p className="text-sm text-gray-500 mt-0.5">{label}</p>
    </div>
  )
}