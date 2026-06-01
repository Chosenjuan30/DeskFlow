import { format } from 'date-fns'
import StatusBadge from './StatusBadge'
import type { TicketStatusUpdate } from '@/modules/ticket/types/ticket.types'

export default function TicketTimeline({ updates }: { updates: TicketStatusUpdate[] }) {
  if (updates.length === 0) {
    return <p className="text-sm text-gray-400 italic">No status changes yet.</p>
  }

  return (
    <ol className="relative border-l border-gray-200 ml-3 space-y-6">
      {updates.map((u) => (
        <li key={u.id} className="ml-6">
          <span className="absolute -left-[9px] flex h-4 w-4 items-center justify-center rounded-full bg-white border-2 border-brand" />

          <div className="flex flex-wrap items-center gap-2 mb-1">
            {u.oldStatus && (
              <>
                <StatusBadge status={u.oldStatus} />
                <span className="text-gray-400 text-xs">→</span>
              </>
            )}
            <StatusBadge status={u.newStatus} />
            <span className="text-xs text-gray-400">
              by {u.changedBy.firstName} {u.changedBy.lastName}
            </span>
            <span className="text-xs text-gray-400">
              {format(new Date(u.createdAt), 'MMM d, yyyy HH:mm')}
            </span>
          </div>

          {u.comment && (
            <p className="text-sm text-gray-600 bg-gray-50 rounded px-3 py-2">{u.comment}</p>
          )}
        </li>
      ))}
    </ol>
  )
}
