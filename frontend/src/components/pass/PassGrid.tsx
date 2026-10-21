import type { EventResponse } from '@/api/types'
import { EventPass } from '@/components/pass/EventPass'

export function PassGrid({ events }: { events: EventResponse[] }) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {events.map((e) => (
        <EventPass key={e.id} event={e} to={`/events/${e.id}`} />
      ))}
    </div>
  )
}
