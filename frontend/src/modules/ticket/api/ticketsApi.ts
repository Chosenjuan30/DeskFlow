import api from '@/shared/api/axiosInstance'
import type { PageResponse } from '@/shared/types/common.types'
import type {
  Ticket,
  CreateTicketPayload,
  UpdateStatusPayload,
  TicketStatus,
} from '@/modules/ticket/types/ticket.types'

interface ListParams {
  status?: TicketStatus
  page?: number
  size?: number
}

export const ticketsApi = {
  createTicket(data: CreateTicketPayload): Promise<Ticket> {
    return api.post<Ticket>('/tickets', data).then(r => r.data)
  },

  getMyTickets(params: ListParams = {}): Promise<PageResponse<Ticket>> {
    return api.get<PageResponse<Ticket>>('/tickets', { params }).then(r => r.data)
  },

  getTicket(id: string): Promise<Ticket> {
    return api.get<Ticket>(`/tickets/${id}`).then(r => r.data)
  },

  updateStatus(id: string, data: UpdateStatusPayload): Promise<Ticket> {
    return api.patch<Ticket>(`/tickets/${id}/status`, data).then(r => r.data)
  },
}
