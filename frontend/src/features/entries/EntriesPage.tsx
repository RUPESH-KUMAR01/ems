import { getEvent } from '@/api/events'
import { getPaymentByRegistration } from '@/api/payments'
import { listMyRegistrations } from '@/api/registrations'
import { EventPass } from '@/components/pass/EventPass'
import { useAuth } from '@/hooks/useAuth'
import { syncFromRegistrations } from '@/lib/entries-store'
import { Button } from '@/components/ui/Button'
import { isProfileComplete, resolveEntryStamp } from '@/lib/ticket-eligibility'
import { useQueries, useQuery } from '@tanstack/react-query'
import { useEffect } from 'react'
import { Link } from 'react-router-dom'

export function EntriesPage() {
  const { user } = useAuth()
  const profileComplete = isProfileComplete(user)

  const registrationsQuery = useQuery({
    queryKey: ['registrations', 'mine', user?.id],
    queryFn: listMyRegistrations,
    enabled: Boolean(user?.id),
  })

  const registrations = registrationsQuery.data ?? []

  useEffect(() => {
    if (user?.id && registrationsQuery.isSuccess) {
      syncFromRegistrations(user.id, registrations)
    }
  }, [user?.id, registrations, registrationsQuery.isSuccess])

  const hydrated = useQueries({
    queries: registrations.map((reg) => ({
      queryKey: ['entries', user?.id, reg.id],
      queryFn: async () => {
        const event = await getEvent(reg.eventId)
        let payment = null
        try {
          payment = await getPaymentByRegistration(reg.id)
        } catch {
          payment = null
        }
        return { registration: reg, event, payment }
      },
      enabled: Boolean(user?.id),
    })),
  })

  if (registrationsQuery.isLoading) {
    return <p className="account-page text-ink-muted">Loading your entries…</p>
  }

  if (registrationsQuery.isError) {
    return <p className="account-page text-signal">Could not load your registrations.</p>
  }

  if (!registrations.length) {
    return (
      <div className="account-page space-y-2">
        <h1 className="text-3xl font-bold">My entries</h1>
        <p className="text-ink-muted text-sm">
          No entries yet. Browse events and complete registration, payment, and profile verification
          to confirm a ticket.
        </p>
        <Link to="/events" className="text-stamp underline text-sm">Browse events</Link>
      </div>
    )
  }

  return (
    <div className="account-page space-y-6">
      <h1 className="text-3xl font-bold">My entries</h1>
      {!profileComplete ? (
        <p className="text-sm text-stamp">
          Verify email and phone on your{' '}
          <Link to="/verify" className="underline">profile</Link> to activate passes.
        </p>
      ) : null}
      <div className="grid gap-4 sm:grid-cols-2">
        {hydrated.map((q, index) => {
          const reg = registrations[index]
          if (!reg) return null
          if (q.isError) {
            return (
              <p key={reg.id} className="text-sm text-signal rounded border border-border p-4">
                Could not load entry #{reg.id}.
              </p>
            )
          }
          if (q.isLoading || !q.data) {
            return (
              <div key={reg.id} className="rounded border border-border p-4 text-sm text-ink-muted">
                Loading pass…
              </div>
            )
          }
          const { registration, event, payment } = q.data
          const fee = Number(event.registrationFee)
          const stamp = resolveEntryStamp(
            fee,
            registration.status,
            payment?.status,
            profileComplete,
          )
          const needsPayment =
            fee > 0 &&
            registration.status === 'PENDING' &&
            payment?.status !== 'COMPLETED'
          return (
            <div key={registration.id} className="space-y-2">
              <Link to={`/entries/${registration.id}`}>
                <EventPass event={event} stamp={stamp} />
              </Link>
              {needsPayment ? (
                <Link to={`/register/${event.id}`}>
                  <Button type="button" className="w-full" variant="secondary">
                    Continue payment
                  </Button>
                </Link>
              ) : null}
            </div>
          )
        })}
      </div>
    </div>
  )
}
