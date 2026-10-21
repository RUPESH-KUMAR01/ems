import type { EventResponse } from '@/api/types'
import type { EventRegistrationResponse, PaymentResponse } from '@/api/types'
import { EventPass } from '@/components/pass/EventPass'
import { PassStamp } from '@/components/pass/PassStamp'
import { formatDate } from '@/lib/format_date'
import {
  getTicketBlockers,
  isProfileComplete,
  resolveEntryStamp,
  ticketStatusMessage,
} from '@/lib/ticket-eligibility'
import type { UserResponse } from '@/api/types'

export function PassPrintView({
  event,
  registration,
  payment,
  user,
}: {
  event: EventResponse
  registration: EventRegistrationResponse
  payment?: PaymentResponse | null
  user?: UserResponse | null
}) {
  const fee = Number(event.registrationFee)
  const profileComplete = isProfileComplete(user)
  const stamp = resolveEntryStamp(
    fee,
    registration.status,
    payment?.status,
    profileComplete,
  )
  const blockers = getTicketBlockers(
    fee,
    registration.status,
    payment?.status,
    profileComplete,
  )
  const statusNote = ticketStatusMessage(blockers)

  return (
    <div className="print-pass mx-auto max-w-md space-y-4">
      <EventPass event={event} stamp={stamp} />
      <div className="rounded-md border border-border bg-surface p-4 text-sm">
        <p className="font-medium">Registration #{registration.id}</p>
        <p className="text-ink-muted tabular">Issued {formatDate(registration.registeredAt)}</p>
        <div className="mt-4 flex flex-col items-center gap-2">
          <PassStamp variant={stamp} />
          {statusNote ? (
            <p className="text-center text-xs text-stamp">{statusNote}</p>
          ) : (
            <p className="text-center text-xs text-mint">Ticket confirmed</p>
          )}
        </div>
      </div>
    </div>
  )
}
