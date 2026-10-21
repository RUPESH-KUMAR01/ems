import type { SystemStatsResponse } from '@/api/types'

function BarChart({ title, data }: { title: string; data: Record<string, number> }) {
  const entries = Object.entries(data)
  const max = Math.max(...entries.map(([, v]) => v), 1)

  return (
    <div className="rounded-lg border border-border p-4">
      <h3 className="font-display text-sm font-semibold mb-3">{title}</h3>
      <ul className="space-y-2">
        {entries.map(([key, value]) => (
          <li key={key} className="text-sm">
            <div className="flex justify-between tabular mb-1">
              <span>{key}</span>
              <span>{value}</span>
            </div>
            <div className="h-2 rounded bg-paper overflow-hidden">
              <div className="h-full bg-stamp" style={{ width: `${(value / max) * 100}%` }} />
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}

export function StatsCharts({ stats }: { stats: SystemStatsResponse }) {
  return (
    <div className="grid gap-4 md:grid-cols-2">
      <BarChart title="Users by role" data={stats.usersByRole} />
      <BarChart title="Events by status" data={stats.eventsByStatus} />
      <BarChart title="Registrations by status" data={stats.registrationsByStatus} />
      <BarChart title="Payments by status" data={stats.paymentsByStatus} />
    </div>
  )
}
