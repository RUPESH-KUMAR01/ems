import { getEvent } from '@/api/events'
import { EventPass } from '@/components/pass/EventPass'
import { Button } from '@/components/ui/Button'
import { isRegistrationOpen } from '@/lib/event-lifecycle'
import { formatDate } from '@/lib/format_date'
import { formatInr } from '@/lib/format_inr'
import { useAuth } from '@/hooks/useAuth'
import { getEntryForEvent } from '@/lib/entries-store'
import { isProfileComplete } from '@/lib/ticket-eligibility'
import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'

export function EventDetailPage() {
  const { eventId } = useParams()
  const id = Number(eventId)
  const { isAuthenticated, user } = useAuth()
  const profileComplete = isProfileComplete(user)

  const { data: event, isLoading, isError } = useQuery({
    queryKey: ['events', id],
    queryFn: () => getEvent(id),
    enabled: Number.isFinite(id),
  })

  if (!Number.isFinite(id)) return <p>Invalid event.</p>
  if (isLoading) return <p className="text-ink-muted">Loading…</p>
  if (isError || !event) return <p className="text-signal">Event not found.</p>

  const registrationOpen = isRegistrationOpen(event)

  const localEntry =
    isAuthenticated && user?.id ? getEntryForEvent(user.id, event.id) : undefined
  const hasPendingPayment = localEntry?.registrationStatus === 'PENDING'
  const isRegistered = localEntry?.registrationStatus === 'REGISTERED'

  return (
    <div className="grid gap-8 lg:grid-cols-[1fr_320px]">
      <div className="space-y-4">
        <EventPass event={event} />
        <div className="prose prose-sm max-w-none text-ink">
          <h2 className="font-display text-xl">About</h2>
          <p className="whitespace-pre-wrap text-ink-muted">{event.description}</p>
          <ul className="mt-4 space-y-1 text-sm text-ink-muted tabular">
            <li>Register by {formatDate(event.registrationDeadline)}</li>
            <li>Capacity up to {event.maxParticipants} participants</li>
            {event.type === 'TEAM' ? (
              <li>Team size {event.minTeamSize}–{event.maxTeamSize}</li>
            ) : null}
          </ul>
        </div>
      </div>
      <aside className="lg:sticky lg:top-24 h-fit rounded-lg border border-border bg-surface p-5">
        <p className="text-2xl font-semibold tabular">
          {Number(event.registrationFee) > 0 ? formatInr(event.registrationFee) : 'Free'}
        </p>
        {event.status === 'CANCELLED' ? (
          <p className="mt-4 text-sm text-stamp">This event was cancelled.</p>
        ) : event.status === 'COMPLETED' ? (
          <p className="mt-4 text-sm text-ink-muted">This event has ended.</p>
        ) : registrationOpen ? (
          isAuthenticated ? (
            <>
              {!profileComplete ? (
                <p className="mt-4 text-sm text-stamp">
                  Verify email and phone on your profile before registering.
                </p>
              ) : null}
              {isRegistered && localEntry ? (
                <Link to={`/entries/${localEntry.registrationId}`} className="mt-4 block">
                  <Button className="w-full" variant="secondary">View your pass</Button>
                </Link>
              ) : hasPendingPayment && localEntry ? (
                <Link to={`/register/${event.id}`} className="mt-4 block">
                  <Button className="w-full" disabled={!profileComplete}>
                    Continue payment
                  </Button>
                </Link>
              ) : (
                <>
                  <Link to={`/register/${event.id}`} className="mt-4 block">
                    <Button className="w-full" disabled={!profileComplete}>
                      {event.type === 'TEAM' ? 'Register (create or join team)' : 'Register for this event'}
                    </Button>
                  </Link>
                  {event.type === 'TEAM' ? (
                    <Link
                      to={`/teams?eventId=${event.id}`}
                      className="mt-2 block text-center text-sm text-stamp underline"
                    >
                      Create a team for this event
                    </Link>
                  ) : null}
                </>
              )}
            </>
          ) : (
            <Link
              to={`/login?redirect=${encodeURIComponent(`/register/${event.id}`)}`}
              className="mt-4 block"
            >
              <Button className="w-full">Log in to register</Button>
            </Link>
          )
        ) : (
          <p className="mt-4 text-sm text-ink-muted">Registration is not open.</p>
        )}
      </aside>
    </div>
  )
}
