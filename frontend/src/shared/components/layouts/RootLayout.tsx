import { Outlet } from 'react-router-dom'
import { QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'sonner'
import { queryClient } from '@/shared/api/queryClient'

/**
 * Top-level layout — provides QueryClient and toast container.
 * All routes nest inside this via React Router's <Outlet />.
 */
export default function RootLayout() {
  return (
    <QueryClientProvider client={queryClient}>
      <Outlet />
      <Toaster richColors position="top-right" closeButton />
    </QueryClientProvider>
  )
}