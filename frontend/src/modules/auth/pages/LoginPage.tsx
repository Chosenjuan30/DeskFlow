import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { toast } from 'sonner'
import { Loader2 } from 'lucide-react'
import { authApi } from '@/modules/auth/api/authApi'
import { useAuthStore } from '@/shared/store/authStore'
import { ROUTES } from '@/router/routes'
import type { UserRole } from '@/shared/types/common.types'

const schema = z.object({
  email:    z.string().email('Enter a valid email'),
  password: z.string().min(1, 'Password is required'),
})

type FormData = z.infer<typeof schema>

const roleHome: Record<UserRole, string> = {
  CUSTOMER:      ROUTES.CUSTOMER_DASHBOARD,
  SUPPORT_AGENT: ROUTES.AGENT_DASHBOARD,
  SUPERVISOR:    ROUTES.SUPERVISOR_DASHBOARD,
  ADMIN:         ROUTES.ADMIN_DASHBOARD,
}

export default function LoginPage() {
  const navigate  = useNavigate()
  const location  = useLocation()
  const { setAuth } = useAuthStore()

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  const onSubmit = async (data: FormData) => {
    try {
      const res = await authApi.login(data)
      setAuth(res.accessToken, res.user)

      const from = (location.state as { from?: Location })?.from?.pathname
      navigate(from ?? roleHome[res.user.role], { replace: true })
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { detail?: string } } })?.response?.data?.detail ??
        'Login failed. Check your credentials.'
      toast.error(msg)
    }
  }

  return (
    <>
      <h2 className="text-xl font-semibold text-gray-900 mb-1">Sign in</h2>
      <p className="text-sm text-gray-500 mb-6">Welcome back to DeskFlow</p>

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
            Email
          </label>
          <input
            id="email"
            type="email"
            autoComplete="email"
            {...register('email')}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand focus:border-transparent disabled:opacity-50"
            disabled={isSubmitting}
          />
          {errors.email && (
            <p className="mt-1 text-xs text-destructive">{errors.email.message}</p>
          )}
        </div>

        <div>
          <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
            Password
          </label>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            {...register('password')}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand focus:border-transparent disabled:opacity-50"
            disabled={isSubmitting}
          />
          {errors.password && (
            <p className="mt-1 text-xs text-destructive">{errors.password.message}</p>
          )}
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full flex items-center justify-center gap-2 bg-brand text-white py-2 px-4 rounded-lg text-sm font-medium hover:bg-brand-dark transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
        >
          {isSubmitting && <Loader2 size={16} className="animate-spin" />}
          {isSubmitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>

      <p className="mt-6 text-center text-sm text-gray-500">
        Don&apos;t have an account?{' '}
        <Link to={ROUTES.REGISTER} className="text-brand hover:underline font-medium">
          Create one
        </Link>
      </p>
    </>
  )
}