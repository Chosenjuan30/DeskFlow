import { useState } from 'react'
import { Link } from 'react-router-dom'
import { format } from 'date-fns'
import { PlusCircle } from 'lucide-react'
import { useMyTickets } from '@/modules/ticket/hooks/useTickets'
import StatusBadge from '@/modules/ticket/components/StatusBadge'
import PriorityBadge from '@/modules/ticket/components/PriorityBadge'
import { ROUTES, buildPath } from '@/router/routes'
import type { TicketStatus } from '@/modules/ticket/types/ticket.types'

const TABS: { label: string; value: TicketStatus | undefined }[] = [
  { label: 'All',              value: undefined },
  { label: 'Open',             value: 'OPEN' },
  { label: 'In Progress',      value: 'IN_PROGRESS' },
  { label: 'Pending',          value: 'PENDING_CUSTOMER' },
  { label: 'Resolved',         value: 'RESOLVED' },
  { label: 'Closed',           value: 'CLOSED' },
]

export default function MyTicketsPage() {
  const [activeStatus, setActiveStatus] = useState<TicketStatus | undefined>(undefined)
  const { data, isLoading } = useMyTickets({ status: activeStatus })

  return (
    <div>
      <div className="flex items-center justify-between mb-5">
        <h1 className="text-xl font-semibold text-gray-900">My Tickets</h1>
        <Link
          to={ROUTES.CUSTOMER_TICKET_NEW}
          className="flex items-center gap-2 px-3 py-2 bg-brand text-white text-sm font-medium rounded-lg hover:bg-brand-dark transition-colors"
        >
          <PlusCircle size={16} />
          New Ticket
        </Link>
      </div>

      {/* Status filter tabs */}
      <div className="flex gap-1 mb-4 border-b border-gray-200">
        {TABS.map((tab) => (
          <button
            key={tab.label}
            onClick={() => setActiveStatus(tab.value)}
            className={`px-3 py-2 text-sm font-medium border-b-2 transition-colors -mb-px ${
              activeStatus === tab.value
                ? 'border-brand text-brand'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12">
          <div className="h-6 w-6 animate-spin rounded-full border-2 border-brand border-t-transparent" />
        </div>
      ) : data?.content.length === 0 ? (
        <div className="text-center py-12 text-gray-400">
          <p className="text-sm">No tickets found.</p>
          <Link to={ROUTES.CUSTOMER_TICKET_NEW} className="mt-2 text-sm text-brand hover:underline inline-block">
            Submit your first ticket
          </Link>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50">
                <th className="text-left px-4 py-3 font-medium text-gray-600">Reference</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Title</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Status</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Priority</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Created</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {data?.content.map((ticket) => (
                <tr key={ticket.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3">
                    <Link
                      to={buildPath(ROUTES.CUSTOMER_TICKET, { id: ticket.id })}
                      className="font-mono text-xs text-brand hover:underline"
                    >
                      {ticket.referenceNumber}
                    </Link>
                  </td>
                  <td className="px-4 py-3">
                    <Link
                      to={buildPath(ROUTES.CUSTOMER_TICKET, { id: ticket.id })}
                      className="text-gray-900 hover:text-brand font-medium truncate max-w-xs block"
                    >
                      {ticket.title}
                    </Link>
                  </td>
                  <td className="px-4 py-3"><StatusBadge status={ticket.status} /></td>
                  <td className="px-4 py-3"><PriorityBadge priority={ticket.priority} /></td>
                  <td className="px-4 py-3 text-gray-500 text-xs">
                    {format(new Date(ticket.createdAt), 'MMM d, yyyy')}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {data && data.totalPages > 1 && (
        <p className="mt-3 text-xs text-gray-400 text-right">
          Page {data.page + 1} of {data.totalPages} — {data.totalElements} tickets total
        </p>
      )}
    </div>
  )
}
