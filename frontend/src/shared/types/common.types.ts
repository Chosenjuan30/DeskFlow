// ── User / Auth ───────────────────────────────────────────────
export type UserRole = 'CUSTOMER' | 'SUPPORT_AGENT' | 'SUPERVISOR' | 'ADMIN'

export interface User {
  id: string
  email: string
  firstName: string
  lastName: string
  role: UserRole
  active: boolean
}

// ── Pagination ────────────────────────────────────────────────
export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

// ── API Errors (RFC 7807 Problem Details) ─────────────────────
export interface ApiError {
  type?: string
  title: string
  status: number
  detail: string
  errors?: Record<string, string>    // validation field errors
}