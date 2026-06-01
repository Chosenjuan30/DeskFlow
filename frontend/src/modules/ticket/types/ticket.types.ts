import type { User, PageResponse } from '@/shared/types/common.types'

export type TicketStatus   = 'OPEN' | 'IN_PROGRESS' | 'PENDING_CUSTOMER' | 'RESOLVED' | 'CLOSED'
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type TicketCategory = 'BILLING' | 'TECHNICAL' | 'ACCOUNT' | 'GENERAL' | 'OTHER'

export interface TicketStatusUpdate {
  id: string
  oldStatus: TicketStatus | null
  newStatus: TicketStatus
  comment: string | null
  changedBy: User
  createdAt: string
}

export interface Ticket {
  id: string
  referenceNumber: string
  title: string
  description: string
  status: TicketStatus
  priority: TicketPriority
  category: TicketCategory
  customer: User
  assignedAgent: User | null
  createdAt: string
  updatedAt: string
  resolvedAt: string | null
  closedAt: string | null
  statusUpdates: TicketStatusUpdate[] | null
}

export interface CreateTicketPayload {
  title: string
  description: string
  priority: TicketPriority
  category: TicketCategory
}

export interface UpdateStatusPayload {
  newStatus: TicketStatus
  comment?: string
}

export type TicketPage = PageResponse<Ticket>
