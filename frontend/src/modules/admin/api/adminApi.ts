import api from '@/shared/api/axiosInstance'

export interface AssignmentDto {
  assignmentId: string
  ticketId: string
  referenceNumber: string
  agentId: string
  agentName: string
  assignedById: string | null
  createdAt: string
}

export const adminApi = {
  assignTicket(ticketId: string, agentId: string): Promise<AssignmentDto> {
    return api.post<AssignmentDto>(`/tickets/${ticketId}/assign`, { agentId }).then(r => r.data)
  },

  reassignTicket(ticketId: string, agentId: string): Promise<AssignmentDto> {
    return api.post<AssignmentDto>(`/tickets/${ticketId}/reassign`, { agentId }).then(r => r.data)
  },
}
