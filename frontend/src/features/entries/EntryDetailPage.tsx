import { getEvent } from '@/api/events'
import { getPaymentByRegistration } from '@/api/payments'
import { listMyRegistrations } from '@/api/registrations'
import { PassPrintView } from '@/components/pass/PassPrintView'
import { Button } from '@/components/ui/Button'
import { useAuth } from '@/hooks/useAuth'
import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'

export function EntryDetailPage() {
  const { user } = useAuth()
  const { registrationId: raw } = useParams()
  const registrationId = Number(raw)

  const registrationsQuery = useQuery({
    queryKey: ['registrations', 'mine', user?.id],
    queryFn: listMyRegistrations,
    enabled: Boolean(user?.id),
  })

  const registration = registrationsQuery.data?.find((r) => r.id === registrationId)

  const eventQuery = useQuery({
    queryKey: ['events', registration?.eventId],
    queryFn: () => getEvent(registration!.eventId),
    enabled: Boolean(registration?.eventId),
  })

  const paymentQuery = useQuery({
    queryKey: ['payments', registrationId],
    queryFn: () => getPaymentByRegistration(registrationId),
    enabled: Number.isFinite(registrationId) && Boolean(registration),
    retry: false,
  })

  if (!Number.isFinite(registrationId)) {
    return <p className="account-page text-signal">Invalid entry ID.</p>
  }

  if (registrationsQuery.isLoading) {
    return <p className="account-page text-ink-muted">Loading pass…</p>
  }

  if (!registration) {
    return (
      <div className="account-page space-y-2">
        <p className="text-ink-muted">This pass is not linked to your account.</p>
        <Link to="/entries" className="text-stamp underline text-sm">Back to my entries</Link>
      </div>
    )
  }

  if (eventQuery.isError) {
    return <p className="account-page text-signal">Could not load event for this pass.</p>
  }

  if (eventQuery.isLoading || !eventQuery.data) {
    return <p className="account-page text-ink-muted">Loading pass…</p>
  }

  return (
    <div className="account-page space-y-6">
      <div className="no-print flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-3xl font-bold">Your pass</h1>
        <Button type="button" variant="secondary" className="shrink-0 self-center" onClick={() => window.print()}>Print</Button>
      </div>
      <PassPrintView
        event={eventQuery.data}
        registration={registration}
        payment={paymentQuery.data}
        user={user}
      />
    </div>
  )
}
