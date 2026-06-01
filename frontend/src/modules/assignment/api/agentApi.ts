import api from '@/shared/api/axiosInstance'
import type { PageResponse } from '@/shared/types/common.types'
import type { Ticket } from '@/modules/ticket/types/ticket.types'

export interface AgentStats {
  activeTickets: number
  resolvedToday: number
  currentLoad: number
}

interface QueueParams {
  page?: number
  size?: number
}

export const agentApi = {
  getAgentQueue(params: QueueParams = {}): Promise<PageResponse<Ticket>> {
    return api.get<PageResponse<Ticket>>('/assignments/my-queue', { params }).then(r => r.data)
  },

  getAgentStats(): Promise<AgentStats> {
    return api.get<AgentStats>('/assignments/my-stats').then(r => r.data)
  },
}
