import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ticketsApi } from '@/modules/ticket/api/ticketsApi'
import type { TicketStatus, UpdateStatusPayload } from '@/modules/ticket/types/ticket.types'

interface ListParams {
  status?: TicketStatus
  page?: number
  size?: number
}

export function useMyTickets(params: ListParams = {}) {
  return useQuery({
    queryKey: ['tickets', 'list', params],
    queryFn:  () => ticketsApi.getMyTickets(params),
  })
}

export function useCreateTicket() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ticketsApi.createTicket,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['tickets'] })
    },
  })
}

export function useUpdateTicketStatus(ticketId: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: UpdateStatusPayload) => ticketsApi.updateStatus(ticketId, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['tickets'] })
    },
  })
}
