import { adminGetStats } from '@/api/admin'
import { StatsCharts } from '@/features/admin/StatsCharts'
import { formatInr } from '@/lib/format_inr'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'

export function AdminDashboardPage() {
  const stats = useQuery({ queryKey: ['admin', 'stats'], queryFn: adminGetStats, retry: false })

  return (
    <div className="space-y-8">
      <h1 className="text-3xl font-bold">Admin</h1>
      <nav className="flex flex-wrap gap-3 text-sm">
        <Link to="/admin/users" className="underline">Users</Link>
        <Link to="/admin/events" className="underline">Events</Link>
        <Link to="/admin/teams" className="underline">Teams</Link>
        <Link to="/admin/registrations" className="underline">Registrations</Link>
        <Link to="/admin/payments" className="underline">Payments</Link>
      </nav>

      {stats.isError ? (
        <p className="text-signal text-sm">Could not load admin stats.</p>
      ) : stats.data ? (
        <>
          <div className="grid grid-cols-2 gap-4 md:grid-cols-4 text-sm tabular">
            <div><p className="text-ink-muted">Users</p><p className="text-2xl font-semibold">{stats.data.totalUsers}</p></div>
            <div><p className="text-ink-muted">Events</p><p className="text-2xl font-semibold">{stats.data.totalEvents}</p></div>
            <div><p className="text-ink-muted">Registrations</p><p className="text-2xl font-semibold">{stats.data.totalRegistrations}</p></div>
            <div><p className="text-ink-muted">Revenue</p><p className="text-2xl font-semibold">{formatInr(stats.data.totalRevenue)}</p></div>
          </div>
          <StatsCharts stats={stats.data} />
        </>
      ) : (
        <p className="text-ink-muted">Loading stats…</p>
      )}
    </div>
  )
}
