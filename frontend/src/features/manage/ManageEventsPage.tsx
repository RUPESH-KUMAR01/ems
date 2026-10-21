import { listMyEvents } from '@/api/events'
import { StatusBadge } from '@/components/StatusBadge'
import { Button } from '@/components/ui/Button'
import { DataTable } from '@/components/DataTable'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'

export function ManageEventsPage() {
  const { data = [], isLoading } = useQuery({ queryKey: ['events', 'me'], queryFn: listMyEvents })

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">Manage events</h1>
        <Link to="/manage/events/new"><Button>Create event</Button></Link>
      </div>
      {isLoading ? (
        <p className="text-ink-muted">Loading…</p>
      ) : (
        <DataTable
          rows={data}
          keyFn={(e) => e.id}
          columns={[
            { header: 'Name', cell: (e) => <Link to={`/manage/events/${e.id}/edit`} className="underline">{e.name}</Link> },
            { header: 'Status', cell: (e) => <StatusBadge status={e.status} /> },
            { header: 'Visibility', cell: (e) => e.visibility },
          ]}
          empty="No events yet."
        />
      )}
    </div>
  )
}
