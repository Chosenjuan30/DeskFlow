import { useParams, useNavigate } from 'react-router-dom'
import { format } from 'date-fns'
import { ArrowLeft, Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import { useTicketDetail } from '@/modules/ticket/hooks/useTicketDetail'
import { useUpdateTicketStatus } from '@/modules/ticket/hooks/useTickets'
import StatusBadge from '@/modules/ticket/components/StatusBadge'
import PriorityBadge from '@/modules/ticket/components/PriorityBadge'
import TicketTimeline from '@/modules/ticket/components/TicketTimeline'
import { ROUTES } from '@/router/routes'

export default function TicketDetailPage() {
  const { id }      = useParams<{ id: string }>()
  const navigate    = useNavigate()
  const { data: ticket, isLoading } = useTicketDetail(id ?? '')
  const closeTicket = useUpdateTicketStatus(id ?? '')

  const handleClose = async () => {
    try {
      await closeTicket.mutateAsync({ newStatus: 'CLOSED', comment: 'Closing ticket' })
      toast.success('Ticket closed.')
    } catch {
      toast.error('Failed to close ticket.')
    }
  }

  if (isLoading) {
    return (
      <div className="flex justify-center py-16">
        <Loader2 size={24} className="animate-spin text-brand" />
      </div>
    )
  }

  if (!ticket) {
    return <p className="text-sm text-gray-500 text-center py-12">Ticket not found.</p>
  }

  return (
    <div className="max-w-3xl mx-auto">
      <button
        onClick={() => navigate(ROUTES.CUSTOMER_TICKETS)}
        className="flex items-center gap-2 text-sm text-gray-500 hover:text-gray-700 mb-5 transition-colors"
      >
        <ArrowLeft size={16} />
        Back to tickets
      </button>

      {/* Header */}
      <div className="bg-white rounded-xl border border-gray-200 p-6 mb-4">
        <div className="flex items-start justify-between gap-4 mb-3">
          <div>
            <span className="font-mono text-xs text-gray-400">{ticket.referenceNumber}</span>
            <h1 className="text-lg font-semibold text-gray-900 mt-0.5">{ticket.title}</h1>
          </div>
          <div className="flex items-center gap-2 flex-shrink-0">
            <PriorityBadge priority={ticket.priority} />
            <StatusBadge status={ticket.status} />
          </div>
        </div>

        <p className="text-sm text-gray-700 whitespace-pre-wrap mb-4">{ticket.description}</p>

        <div className="grid grid-cols-2 gap-x-8 gap-y-2 text-xs text-gray-500 border-t border-gray-100 pt-4">
          <div><span className="font-medium text-gray-700">Category: </span>{ticket.category}</div>
          <div><span className="font-medium text-gray-700">Submitted: </span>
            {format(new Date(ticket.createdAt), 'MMM d, yyyy HH:mm')}
          </div>
          {ticket.assignedAgent && (
            <div>
              <span className="font-medium text-gray-700">Assigned to: </span>
              {ticket.assignedAgent.firstName} {ticket.assignedAgent.lastName}
            </div>
          )}
          {ticket.resolvedAt && (
            <div><span className="font-medium text-gray-700">Resolved: </span>
              {format(new Date(ticket.resolvedAt), 'MMM d, yyyy HH:mm')}
            </div>
          )}
        </div>

        {ticket.status === 'RESOLVED' && (
          <div className="mt-4 pt-4 border-t border-gray-100">
            <p className="text-sm text-gray-600 mb-3">
              Your issue has been resolved. If everything looks good, you can close this ticket.
            </p>
            <button
              onClick={handleClose}
              disabled={closeTicket.isPending}
              className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 transition-colors disabled:opacity-60"
            >
              {closeTicket.isPending && <Loader2 size={14} className="animate-spin" />}
              Close ticket
            </button>
          </div>
        )}
      </div>

      {/* Timeline */}
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <h2 className="text-sm font-semibold text-gray-900 mb-4">Status history</h2>
        <TicketTimeline updates={ticket.statusUpdates ?? []} />
      </div>
    </div>
  )
}
