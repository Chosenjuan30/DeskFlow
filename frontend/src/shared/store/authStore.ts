import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { User } from '@/shared/types/common.types'

interface AuthState {
  accessToken: string | null
  user: User | null

  setAuth: (token: string, user: User) => void
  setToken: (token: string) => void
  clearAuth: () => void
  isAuthenticated: () => boolean
}

/**
 * Global auth store.
 * Persisted to localStorage so the session survives page reloads.
 * In production, consider HttpOnly cookie for the refresh token
 * and keeping only the access token in memory.
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      user: null,

      setAuth: (token, user) => set({ accessToken: token, user }),
      setToken: (token) => set({ accessToken: token }),
      clearAuth: () => set({ accessToken: null, user: null }),
      isAuthenticated: () => !!get().accessToken,
    }),
    {
      name: 'deskflow-auth',
      partialize: (state) => ({
        accessToken: state.accessToken,
        user: state.user,
      }),
    }
  )
)