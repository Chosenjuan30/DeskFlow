import { cn } from '@/lib/utils'
import type { TicketStatus } from '@/modules/ticket/types/ticket.types'

const config: Record<TicketStatus, { label: string; classes: string }> = {
  OPEN:             { label: 'Open',             classes: 'bg-gray-100 text-gray-700' },
  IN_PROGRESS:      { label: 'In Progress',      classes: 'bg-blue-100 text-blue-700' },
  PENDING_CUSTOMER: { label: 'Pending Customer', classes: 'bg-amber-100 text-amber-700' },
  RESOLVED:         { label: 'Resolved',         classes: 'bg-green-100 text-green-700' },
  CLOSED:           { label: 'Closed',           classes: 'bg-slate-100 text-slate-500' },
}

export default function StatusBadge({ status }: { status: TicketStatus }) {
  const { label, classes } = config[status]
  return (
    <span className={cn('inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium', classes)}>
      {label}
    </span>
  )
}
