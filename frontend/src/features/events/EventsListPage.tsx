import { listVisibleEvents } from '@/api/events'
import { PassGrid } from '@/components/pass/PassGrid'
import { PassSkeleton } from '@/components/pass/PassSkeleton'
import { FilterBar, FilterSheet, useEventFilters } from '@/features/events/FilterBar'
import { useQuery } from '@tanstack/react-query'

export function EventsListPage() {
  const { data = [], isLoading, isError } = useQuery({
    queryKey: ['events', 'visible'],
    queryFn: listVisibleEvents,
  })
  const { filters, setFilters, filtered } = useEventFilters(data)

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Events</h1>
      <FilterBar filters={filters} onChange={setFilters} />
      <FilterSheet filters={filters} onChange={setFilters} />
      {isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2">
          <PassSkeleton />
          <PassSkeleton />
        </div>
      ) : isError ? (
        <p className="text-signal">Failed to load events.</p>
      ) : filtered.length ? (
        <PassGrid events={filtered} />
      ) : (
        <p className="text-ink-muted">No events match your filters.</p>
      )}
    </div>
  )
}
