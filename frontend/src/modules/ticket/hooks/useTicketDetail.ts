import { useQuery } from '@tanstack/react-query'
import { ticketsApi } from '@/modules/ticket/api/ticketsApi'

export function useTicketDetail(id: string) {
  return useQuery({
    queryKey: ['tickets', id],
    queryFn:  () => ticketsApi.getTicket(id),
    enabled:  !!id,
  })
}
