import type { EventType } from '@/api/types'

export function paymentStepIndex(type: EventType): number {
  return type === 'TEAM' ? 3 : 2
}

export function doneStepIndex(type: EventType): number {
  return type === 'TEAM' ? 4 : 3
}

export function reviewStepIndex(type: EventType): number {
  return type === 'TEAM' ? 2 : 1
}
