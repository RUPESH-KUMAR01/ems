import type { EventResponse, EventStatus } from '@/api/types'
import { toEpochMs } from '@/lib/iso-datetime'

export type LifecycleAction = 'publish' | 'cancel' | 'complete'

export function isRegistrationOpen(event: Pick<EventResponse, 'status' | 'registrationDeadline'>): boolean {
  if (event.status !== 'PUBLISHED') return false
  const deadlineMs = toEpochMs(event.registrationDeadline)
  if (deadlineMs == null) return false
  return deadlineMs > Date.now()
}

export function allowedLifecycleActions(status: EventStatus): LifecycleAction[] {
  switch (status) {
    case 'DRAFT':
      return ['publish']
    case 'PUBLISHED':
      return ['cancel', 'complete']
    case 'CANCELLED':
    case 'COMPLETED':
      return []
    default:
      return []
  }
}

export function lifecycleSuccessMessage(action: LifecycleAction): string {
  switch (action) {
    case 'publish':
      return 'Event published'
    case 'cancel':
      return 'Event cancelled'
    case 'complete':
      return 'Event marked complete'
  }
}
