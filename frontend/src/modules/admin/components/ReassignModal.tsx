import { useState } from 'react'
import { X, Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import { useQueryClient } from '@tanstack/react-query'
import { adminApi } from '@/modules/admin/api/adminApi'

interface Props {
  ticketId: string
  onClose: () => void
}

export default function ReassignModal({ ticketId, onClose }: Props) {
  const [agentId, setAgentId] = useState('')
  const [loading, setLoading] = useState(false)
  const qc = useQueryClient()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!agentId.trim()) return
    setLoading(true)
    try {
      await adminApi.reassignTicket(ticketId, agentId.trim())
      toast.success('Ticket reassigned.')
      qc.invalidateQueries({ queryKey: ['tickets'] })
      qc.invalidateQueries({ queryKey: ['agent'] })
      onClose()
    } catch {
      toast.error('Reassignment failed.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-sm mx-4 p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-sm font-semibold text-gray-900">Reassign ticket</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            <X size={16} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">Agent ID</label>
            <input
              type="text"
              value={agentId}
              onChange={e => setAgentId(e.target.value)}
              placeholder="Paste agent UUID"
              className="w-full text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-brand/40"
            />
            <p className="text-xs text-gray-400 mt-1">
              Agent selector UI coming in a future phase.
            </p>
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-3 py-1.5 text-sm text-gray-600 hover:text-gray-900 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || !agentId.trim()}
              className="flex items-center gap-1.5 px-4 py-1.5 bg-brand text-white text-sm font-medium rounded-lg hover:bg-brand-dark transition-colors disabled:opacity-60"
            >
              {loading && <Loader2 size={14} className="animate-spin" />}
              Reassign
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
