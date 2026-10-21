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

  if (!Number.isFinite(id)) return <div className="event-detail-state">Invalid event.</div>
  if (isLoading) return <div className="event-detail-state">Finding the details…</div>
  if (isError || !event) return <div className="event-detail-state">Event not found.</div>

  const registrationOpen = isRegistrationOpen(event)
  const localEntry = isAuthenticated && user?.id ? getEntryForEvent(user.id, event.id) : undefined
  const hasPendingPayment = localEntry?.registrationStatus === 'PENDING'
  const isRegistered = localEntry?.registrationStatus === 'REGISTERED'

  return (
    <div className="event-detail-page">
      <div className="event-detail-breadcrumb"><Link to="/events">← All opportunities</Link><span> / EVENT BRIEF / {String(event.id).padStart(3, '0')}</span></div>
      <div className="event-detail-layout">
        <main className="event-detail-main">
          <div className="event-detail-card"><EventPass event={event} /></div>
          <section className="event-brief-section">
            <p className="market-kicker">THE BRIEF</p><h2>About this opportunity<span>.</span></h2>
            <p className="event-detail-description">{event.description}</p>
          </section>
          <section className="event-facts">
            <div><span>REGISTRATION DEADLINE</span><strong>{formatDate(event.registrationDeadline)}</strong></div>
            <div><span>PARTICIPANT CAPACITY</span><strong>Up to {event.maxParticipants}</strong></div>
            <div><span>PARTICIPATION</span><strong>{event.type === 'TEAM' ? 'Team challenge' : 'Individual entry'}</strong></div>
            {event.type === 'TEAM' ? <div><span>TEAM SIZE</span><strong>{event.minTeamSize}–{event.maxTeamSize} members</strong></div> : null}
          </section>
        </main>
        <aside className="event-registration-panel">
          <p className="market-kicker">YOUR NEXT MOVE</p>
          <span className="registration-price">{Number(event.registrationFee) > 0 ? formatInr(event.registrationFee) : 'Free'}</span>
          <span className="registration-price-caption">ENTRY FEE / PER REGISTRATION</span>
          {event.status === 'CANCELLED' ? (
            <p className="registration-status">This event was cancelled.</p>
          ) : event.status === 'COMPLETED' ? (
            <p className="registration-status">This event has ended.</p>
          ) : registrationOpen ? (
            isAuthenticated ? (
              <>
                {!profileComplete ? <p className="registration-status">Verify email and phone on your profile before registering.</p> : null}
                {isRegistered && localEntry ? (
                  <Link to={`/entries/${localEntry.registrationId}`} className="registration-action"><Button className="w-full" variant="secondary">View your pass ↗</Button></Link>
                ) : hasPendingPayment && localEntry ? (
                  <Link to={`/register/${event.id}`} className="registration-action"><Button className="w-full" disabled={!profileComplete}>Continue payment ↗</Button></Link>
                ) : (
                  <>
                    <Link to={`/register/${event.id}`} className="registration-action"><Button className="w-full" disabled={!profileComplete}>{event.type === 'TEAM' ? 'Register / create or join team ↗' : 'Register for this event ↗'}</Button></Link>
                    {event.type === 'TEAM' ? <Link to={`/teams?eventId=${event.id}`} className="registration-team-link">Create a team for this event →</Link> : null}
                  </>
                )}
              </>
            ) : (
              <Link to={`/login?redirect=${encodeURIComponent(`/register/${event.id}`)}`} className="registration-action"><Button className="w-full">Log in to register ↗</Button></Link>
            )
          ) : (
            <p className="registration-status">Registration is not open.</p>
          )}
          <div className="registration-reassurance"><span>✳</span><p>Your registration, payment status, and event pass live together in My Passes.</p></div>
        </aside>
      </div>
    </div>
  )
}
