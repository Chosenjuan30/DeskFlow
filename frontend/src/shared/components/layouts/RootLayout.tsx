import { useEffect } from 'react'
import { Outlet } from 'react-router-dom'
import { QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'sonner'
import { queryClient } from '@/shared/api/queryClient'
import { authApi } from '@/modules/auth/api/authApi'
import { useAuthStore } from '@/shared/store/authStore'

function AppInitializer() {
  const { accessToken, setAuth, clearAuth } = useAuthStore()

  useEffect(() => {
    if (!accessToken) return
    authApi.me()
      .then((user) => setAuth(accessToken, user))
      .catch(() => clearAuth())
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  return null
}

/**
 * Top-level layout — provides QueryClient and toast container.
 * AppInitializer re-validates the stored token on every page load.
 */
export default function RootLayout() {
  return (
    <QueryClientProvider client={queryClient}>
      <AppInitializer />
      <Outlet />
      <Toaster richColors position="top-right" closeButton />
    </QueryClientProvider>
  )
}