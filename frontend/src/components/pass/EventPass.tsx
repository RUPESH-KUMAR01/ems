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

function deadlineLabel(deadline: string) {
  const date = new Date(deadline)
  if (Number.isNaN(date.getTime())) return formatDateShort(deadline)
  const days = Math.ceil((date.getTime() - Date.now()) / 86400000)
  if (days < 0) return 'Registration closed'
  if (days === 0) return 'Closes today'
  if (days === 1) return '1 day left'
  if (days <= 7) return `${days} days left`
  return formatDateShort(deadline)
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
  const teaser = event.description.trim().slice(0, 112)
  const fee = Number(event.registrationFee)
  const closed = ['CANCELLED', 'COMPLETED'].includes(event.status) || new Date(event.registrationDeadline).getTime() < Date.now()
  const body = (
    <article className={cn('event-opportunity-card group', closed && 'event-opportunity-closed', className)}>
      <div className="opportunity-art">
        <div className={cn('opportunity-art-shape', event.type === 'TEAM' ? 'shape-team' : 'shape-solo')}>
          <span>{event.type === 'TEAM' ? 'T' : 'S'}</span>
        </div>
        <span className="opportunity-category">{event.type === 'TEAM' ? 'TEAM CHALLENGE' : 'INDIVIDUAL'}</span>
        <span className="opportunity-stamp"><PassStamp variant={stamp ?? stampFromFee(fee, event.status)} /></span>
        <span className="opportunity-art-index">EMS / {String(event.id).padStart(3, '0')}</span>
      </div>
      <div className="opportunity-body">
        <div className="opportunity-meta"><span>{event.type === 'TEAM' ? 'Built together' : 'Made for you'}</span><span className="opportunity-dot">·</span><span>{deadlineLabel(event.registrationDeadline)}</span></div>
        <h3>{event.name}</h3>
        <p className="opportunity-description">{teaser}{event.description.length > 112 ? '…' : ''}</p>
        <div className="opportunity-footer">
          <div className="opportunity-fee"><span>ENTRY</span><strong>{fee > 0 ? formatInr(event.registrationFee) : 'Free'}</strong></div>
          <span className="opportunity-open" aria-hidden="true">↗</span>
        </div>
      </div>
    </article>
  )

  if (to) return <Link to={to} className="event-opportunity-link">{body}</Link>
  return body
}
