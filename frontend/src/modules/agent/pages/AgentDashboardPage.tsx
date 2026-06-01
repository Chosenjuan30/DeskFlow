import { Link } from 'react-router-dom'
import { format } from 'date-fns'
import { Inbox, CheckCircle, BarChart2 } from 'lucide-react'
import { useAgentQueue, useAgentStats } from '@/modules/assignment/hooks/useAgentQueue'
import StatusBadge from '@/modules/ticket/components/StatusBadge'
import PriorityBadge from '@/modules/ticket/components/PriorityBadge'
import { buildPath, ROUTES } from '@/router/routes'
import { useAuthStore } from '@/shared/store/authStore'

export default function AgentDashboardPage() {
  const { user } = useAuthStore()
  const { data: queue } = useAgentQueue({ size: 10 })
  const { data: stats } = useAgentStats()

  return (
    <div>
      <h1 className="text-xl font-semibold text-gray-900 mb-1">
        Welcome back, {user?.firstName}
      </h1>
      <p className="text-sm text-gray-500 mb-6">Your active ticket queue.</p>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
        <StatCard
          icon={<Inbox size={20} className="text-brand" />}
          label="Active tickets"
          value={stats?.activeTickets ?? '—'}
          bg="bg-brand-light"
        />
        <StatCard
          icon={<CheckCircle size={20} className="text-green-600" />}
          label="Resolved today"
          value={stats?.resolvedToday ?? '—'}
          bg="bg-green-50"
        />
        <StatCard
          icon={<BarChart2 size={20} className="text-amber-500" />}
          label="Current load"
          value={stats?.currentLoad ?? '—'}
          bg="bg-amber-50"
        />
      </div>

      {/* Queue */}
      <div className="bg-white rounded-xl border border-gray-200">
        <div className="px-5 py-4 border-b border-gray-100">
          <h2 className="text-sm font-semibold text-gray-900">My queue</h2>
        </div>

        {queue?.content.length === 0 ? (
          <div className="py-10 text-center text-gray-400 text-sm">
            No tickets assigned to you.
          </div>
        ) : (
          <ul className="divide-y divide-gray-100">
            {queue?.content.map((ticket) => (
              <li key={ticket.id} className="px-5 py-3 hover:bg-gray-50 transition-colors">
                <Link to={buildPath(ROUTES.AGENT_TICKET, { id: ticket.id })} className="flex items-center gap-3">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-0.5">
                      <span className="font-mono text-xs text-gray-400">{ticket.referenceNumber}</span>
                      <StatusBadge status={ticket.status} />
                      <PriorityBadge priority={ticket.priority} />
                    </div>
                    <p className="text-sm font-medium text-gray-900 truncate">{ticket.title}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{ticket.customer.firstName} {ticket.customer.lastName}</p>
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
