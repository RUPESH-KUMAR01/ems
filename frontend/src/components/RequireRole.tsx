import { useAuth } from '@/hooks/useAuth'
import type { Role } from '@/api/types'
import { Navigate, useLocation } from 'react-router-dom'

export function RequireRole({
  minimum,
  adminOnly,
  children,
}: {
  minimum?: Role
  adminOnly?: boolean
  children: React.ReactNode
}) {
  const { isAuthenticated, isLoading, user, hasRole } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return <p className="p-8 text-sm text-ink-muted">Loading session…</p>
  }

  if (!isAuthenticated) {
    return <Navigate to={`/login?redirect=${encodeURIComponent(location.pathname)}`} replace />
  }

  if (adminOnly && user?.role !== 'ADMIN') {
    return <Navigate to="/403" replace />
  }

  if (minimum && !hasRole(minimum)) {
    return <Navigate to="/403" replace />
  }

  return children
}
