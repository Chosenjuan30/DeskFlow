/**
 * Central route constants — import these instead of raw strings
 * so route renames propagate everywhere automatically.
 */
export const ROUTES = {
  // Auth
  LOGIN:    '/login',
  REGISTER: '/register',

  // Customer
  CUSTOMER_DASHBOARD:   '/customer/dashboard',
  CUSTOMER_TICKETS:     '/customer/tickets',
  CUSTOMER_TICKET_NEW:  '/customer/tickets/new',
  CUSTOMER_TICKET:      '/customer/tickets/:id',

  // Agent
  AGENT_DASHBOARD: '/agent/dashboard',
  AGENT_TICKET:    '/agent/tickets/:id',

  // Supervisor
  SUPERVISOR_DASHBOARD:   '/supervisor/dashboard',
  SUPERVISOR_ESCALATIONS: '/supervisor/escalations',
  SUPERVISOR_SLA:         '/supervisor/sla',

  // Admin
  ADMIN_DASHBOARD: '/admin/dashboard',
  ADMIN_USERS:     '/admin/users',
  ADMIN_SLA:       '/admin/sla',
  ADMIN_ANALYTICS: '/admin/analytics',
} as const

/** Build a concrete path from a parameterised route constant */
export function buildPath(route: string, params: Record<string, string> = {}): string {
  return Object.entries(params).reduce(
    (path, [key, val]) => path.replace(`:${key}`, val),
    route
  )
}