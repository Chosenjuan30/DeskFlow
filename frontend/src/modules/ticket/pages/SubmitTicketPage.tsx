import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Loader2 } from 'lucide-react'
import { useCreateTicket } from '@/modules/ticket/hooks/useTickets'
import { ROUTES, buildPath } from '@/router/routes'
import type { TicketPriority, TicketCategory } from '@/modules/ticket/types/ticket.types'

const schema = z.object({
  title:       z.string().min(3, 'Title must be at least 3 characters').max(255),
  description: z.string().min(10, 'Please provide more detail (at least 10 characters)'),
  priority:    z.enum(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] as [TicketPriority, ...TicketPriority[]]),
  category:    z.enum(['BILLING', 'TECHNICAL', 'ACCOUNT', 'GENERAL', 'OTHER'] as [TicketCategory, ...TicketCategory[]]),
})

type FormData = z.infer<typeof schema>

export default function SubmitTicketPage() {
  const navigate     = useNavigate()
  const createTicket = useCreateTicket()

  const { register, handleSubmit, formState: { errors, isSubmitting } } =
    useForm<FormData>({ resolver: zodResolver(schema), defaultValues: { priority: 'MEDIUM', category: 'GENERAL' } })

  const onSubmit = async (data: FormData) => {
    try {
      const ticket = await createTicket.mutateAsync(data)
      toast.success(`Ticket ${ticket.referenceNumber} submitted successfully.`)
      navigate(buildPath(ROUTES.CUSTOMER_TICKET, { id: ticket.id }))
    } catch {
      toast.error('Failed to submit ticket. Please try again.')
    }
  }

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-xl font-semibold text-gray-900 mb-1">Submit a support ticket</h1>
      <p className="text-sm text-gray-500 mb-6">Describe your issue and our team will respond promptly.</p>

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-5 bg-white rounded-xl border border-gray-200 p-6">

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Title</label>
          <input
            type="text"
            placeholder="Brief summary of your issue"
            {...register('title')}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand"
          />
          {errors.title && <p className="mt-1 text-xs text-destructive">{errors.title.message}</p>}
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea
            rows={5}
            placeholder="Describe your issue in detail…"
            {...register('description')}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand resize-none"
          />
          {errors.description && <p className="mt-1 text-xs text-destructive">{errors.description.message}</p>}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
            <select
              {...register('category')}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand bg-white"
            >
              <option value="BILLING">Billing</option>
              <option value="TECHNICAL">Technical</option>
              <option value="ACCOUNT">Account</option>
              <option value="GENERAL">General</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Priority</label>
            <select
              {...register('priority')}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand bg-white"
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="CRITICAL">Critical</option>
            </select>
          </div>
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={() => navigate(ROUTES.CUSTOMER_TICKETS)}
            className="px-4 py-2 text-sm font-medium text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isSubmitting}
            className="flex items-center gap-2 px-4 py-2 bg-brand text-white text-sm font-medium rounded-lg hover:bg-brand-dark transition-colors disabled:opacity-60"
          >
            {isSubmitting && <Loader2 size={14} className="animate-spin" />}
            {isSubmitting ? 'Submitting…' : 'Submit ticket'}
          </button>
        </div>
      </form>
    </div>
  )
}
