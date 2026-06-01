import api from '@/shared/api/axiosInstance'
import type { User } from '@/shared/types/common.types'

const REFRESH_KEY = 'deskflow-refresh'

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: User
}

export interface LoginPayload {
  email: string
  password: string
}

export interface RegisterPayload {
  email: string
  password: string
  firstName: string
  lastName: string
}

function storeRefreshToken(token: string) {
  localStorage.setItem(REFRESH_KEY, token)
}

function clearRefreshToken() {
  localStorage.removeItem(REFRESH_KEY)
}

export const authApi = {
  async login(payload: LoginPayload): Promise<AuthResponse> {
    const { data } = await api.post<AuthResponse>('/auth/login', payload)
    storeRefreshToken(data.refreshToken)
    return data
  },

  async register(payload: RegisterPayload): Promise<AuthResponse> {
    const { data } = await api.post<AuthResponse>('/auth/register', payload)
    storeRefreshToken(data.refreshToken)
    return data
  },

  async refresh(): Promise<AuthResponse> {
    const refreshToken = localStorage.getItem(REFRESH_KEY)
    const { data } = await api.post<AuthResponse>('/auth/refresh', { refreshToken })
    storeRefreshToken(data.refreshToken)
    return data
  },

  async logout(): Promise<void> {
    try {
      await api.post('/auth/logout')
    } finally {
      clearRefreshToken()
    }
  },

  async me(): Promise<User> {
    const { data } = await api.get<User>('/auth/me')
    return data
  },
}