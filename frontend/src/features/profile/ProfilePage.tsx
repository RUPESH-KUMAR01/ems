import { useAuth } from '@/hooks/useAuth'
import { Link } from 'react-router-dom'
import { StatusBadge } from '@/components/StatusBadge'

export function ProfilePage() {
  const { user, isLoading } = useAuth()
  if (isLoading) return <p className="text-ink-muted">Loading profile…</p>
  if (!user) return <p className="text-signal">Could not load profile.</p>

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <h1 className="text-3xl font-bold">Profile</h1>
      <dl className="space-y-2 text-sm">
        <div><dt className="text-ink-muted">Name</dt><dd className="font-medium">{user.name}</dd></div>
        <div><dt className="text-ink-muted">Email</dt><dd>{user.email}</dd></div>
        <div className="flex items-center gap-2">
          <dt className="text-ink-muted">Role</dt>
          <dd><StatusBadge status={user.role} /></dd>
        </div>
        <div>
          <dt className="text-ink-muted">Verification</dt>
          <dd className="mt-1 space-x-2">
            <StatusBadge status={user.emailVerified ? 'APPROVED' : 'PENDING'} />
            <StatusBadge status={user.phoneVerified ? 'APPROVED' : 'PENDING'} />
          </dd>
        </div>
      </dl>
      <Link to="/verify" className="text-stamp underline text-sm">Manage verification</Link>
    </div>
  )
}
