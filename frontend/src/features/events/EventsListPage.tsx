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
    <div className="opportunities-page">
      <section className="opportunities-heading">
        <div>
          <p className="market-kicker">THE OPEN CALL BOARD / 2026</p>
          <h1>Find your next<br /><em>big thing.</em></h1>
          <p className="opportunities-lede">A collection of ways to learn, compete, build, and meet people who get it.</p>
        </div>
        <div className="opportunities-count"><strong>{isLoading ? '—' : filtered.length.toString().padStart(2, '0')}</strong><span>OPEN OPPORTUNITIES<br />IN YOUR VIEW</span></div>
      </section>
      <section className="opportunities-controls" aria-label="Filter opportunities">
        <div className="opportunities-controls-title"><span>01 / DISCOVER</span><span>FIND YOUR FIT</span></div>
        <FilterBar filters={filters} onChange={setFilters} />
        <FilterSheet filters={filters} onChange={setFilters} />
      </section>
      <section className="opportunities-results">
        <div className="opportunities-results-head"><h2>{filters.search ? 'Search results' : 'All opportunities'}<span> ↘</span></h2><p>Good things happen when you show up.</p></div>
        {isLoading ? (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3"><PassSkeleton /><PassSkeleton /><PassSkeleton /></div>
        ) : isError ? (
          <div className="market-empty"><span className="empty-symbol">↗</span><h3>We lost the signal.</h3><p>Events couldn't load right now. Try again in a moment.</p></div>
        ) : filtered.length ? (
          <PassGrid events={filtered} />
        ) : (
          <div className="market-empty"><span className="empty-symbol">⌕</span><h3>No matches just yet.</h3><p>Try a different search or loosen your filters to see more opportunities.</p></div>
        )}
      </section>
    </div>
  )
}
