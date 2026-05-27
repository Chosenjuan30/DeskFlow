import axios from 'axios'
import { useAuthStore } from '@/shared/store/authStore'

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

const api = axios.create({
  baseURL: `${BASE_URL}/api/v1`,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15_000,
})

// ── Request interceptor — attach Bearer token ─────────────────
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ── Response interceptor — auto-refresh on 401 ───────────────
let refreshPromise: Promise<string> | null = null

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Only attempt refresh once per request (avoid infinite loops)
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        // Deduplicate concurrent 401s — only one refresh call at a time
        if (!refreshPromise) {
          refreshPromise = axios
            .post(`${BASE_URL}/api/v1/auth/refresh`, {
              refreshToken: localStorage.getItem('deskflow-refresh'),
            })
            .then((res) => res.data.accessToken)
            .finally(() => { refreshPromise = null })
        }

        const newToken = await refreshPromise
        useAuthStore.getState().setToken(newToken)
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return api(originalRequest)
      } catch {
        // Refresh failed — force logout
        useAuthStore.getState().clearAuth()
        window.location.href = '/login'
      }
    }

    return Promise.reject(error)
  }
)

export default api