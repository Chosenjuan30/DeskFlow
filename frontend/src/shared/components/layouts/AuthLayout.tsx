import { Outlet } from 'react-router-dom'

/**
 * Centred card shell used by Login and Register pages.
 */
export default function AuthLayout() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 px-4">
      <div className="mb-8 text-center">
        <div className="flex items-center justify-center gap-2 mb-2">
          {/* Simple icon placeholder — replace with SVG logo in Phase 8 */}
          <div className="w-8 h-8 rounded-lg bg-brand flex items-center justify-center">
            <span className="text-white font-bold text-sm">D</span>
          </div>
          <h1 className="text-2xl font-bold text-gray-900 tracking-tight">DeskFlow</h1>
        </div>
        <p className="text-sm text-gray-500">Customer Support Platform</p>
      </div>

      <div className="w-full max-w-md bg-white rounded-xl shadow-sm border border-gray-200 p-8">
        <Outlet />
      </div>
    </div>
  )
}