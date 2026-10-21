import type { EventResponse } from '@/api/types'
import { PassStamp, type StampVariant } from '@/components/pass/PassStamp'
import { formatDateShort } from '@/lib/format_date'
import { formatInr } from '@/lib/format_inr'
import { cn } from '@/lib/cn'
import { Link } from 'react-router-dom'

function stampFromFee(fee: number, status?: string): StampVariant {
  if (status === 'DRAFT') return 'draft'
  if (status === 'CANCELLED') return 'cancelled'
  if (status === 'COMPLETED') return 'paid'
  if (fee <= 0) return 'free'
  return 'pending'
}

export function EventPass({
  event,
  stamp,
  className,
  to,
}: {
  event: EventResponse
  stamp?: StampVariant
  className?: string
  to?: string
}) {
  const teaser = event.description.trim().slice(0, 80)
  const body = (
    <article
      className={cn(
        'relative overflow-hidden rounded-lg border border-border bg-surface shadow-[0_8px_24px_var(--shadow)]',
        'before:absolute before:left-0 before:top-1/2 before:h-8 before:w-4 before:-translate-x-1/2 before:-translate-y-1/2 before:rounded-full before:bg-paper',
        className,
      )}
    >
      <div className="border-b border-dashed border-border px-5 py-4">
        <PassStamp variant={stamp ?? stampFromFee(Number(event.registrationFee), event.status)} />
        <h3 className="font-display text-xl font-semibold pr-20">{event.name}</h3>
        <p className="mt-1 text-sm text-ink-muted tabular">{formatDateShort(event.registrationDeadline)}</p>
      </div>
      <div className="grid gap-2 px-5 py-4 text-sm">
        <p className="text-ink-muted line-clamp-2">{teaser}{event.description.length > 80 ? '…' : ''}</p>
        <div className="flex items-center justify-between tabular">
          <span className="text-ink-muted">{event.type === 'TEAM' ? 'Team event' : 'Solo'}</span>
          <span className="font-medium">
            {Number(event.registrationFee) > 0
              ? formatInr(event.registrationFee)
              : 'No fee'}
          </span>
        </div>
      </div>
    </article>
  )

  if (to) {
    return (
      <Link to={to} className="block transition hover:-translate-y-0.5">
        {body}
      </Link>
    )
  }
  return body
}
