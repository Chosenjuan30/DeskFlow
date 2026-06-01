import { useQuery } from '@tanstack/react-query'
import { agentApi } from '@/modules/assignment/api/agentApi'

export function useAgentQueue(params: { page?: number; size?: number } = {}) {
  return useQuery({
    queryKey: ['agent', 'queue', params],
    queryFn:  () => agentApi.getAgentQueue(params),
  })
}

export function useAgentStats() {
  return useQuery({
    queryKey: ['agent', 'stats'],
    queryFn:  agentApi.getAgentStats,
    refetchInterval: 30_000,
  })
}
