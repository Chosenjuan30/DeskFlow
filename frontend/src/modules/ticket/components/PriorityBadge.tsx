import { cn } from '@/lib/utils'
import type { TicketPriority } from '@/modules/ticket/types/ticket.types'

const config: Record<TicketPriority, { label: string; classes: string }> = {
  LOW:      { label: 'Low',      classes: 'bg-gray-100 text-gray-600' },
  MEDIUM:   { label: 'Medium',   classes: 'bg-blue-100 text-blue-700' },
  HIGH:     { label: 'High',     classes: 'bg-orange-100 text-orange-700' },
  CRITICAL: { label: 'Critical', classes: 'bg-red-100 text-red-700' },
}

export default function PriorityBadge({ priority }: { priority: TicketPriority }) {
  const { label, classes } = config[priority]
  return (
    <span className={cn('inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium', classes)}>
      {label}
    </span>
  )
}
