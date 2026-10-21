import type {
  PaymentStatus,
  RegistrationStatus,
  UserResponse,
} from '@/api/types'
import type { StampVariant } from '@/components/pass/PassStamp'

export function isProfileComplete(user: UserResponse | null | undefined): boolean {
  return Boolean(user?.emailVerified && user?.phoneVerified)
}

export type TicketBlocker = 'profile' | 'payment' | 'registration' | 'cancelled'

export function getTicketBlockers(
  fee: number,
  registrationStatus: RegistrationStatus,
  paymentStatus: PaymentStatus | undefined,
  profileComplete: boolean,
): TicketBlocker[] {
  const blockers: TicketBlocker[] = []
  if (registrationStatus === 'CANCELLED') {
    blockers.push('cancelled')
    return blockers
  }
  if (!profileComplete) blockers.push('profile')
  if (fee > 0) {
    if (paymentStatus !== 'COMPLETED' && registrationStatus !== 'REGISTERED') {
      blockers.push('payment')
    }
  } else if (registrationStatus !== 'REGISTERED') {
    blockers.push('registration')
  }
  return blockers
}

export function isTicketConfirmed(
  fee: number,
  registrationStatus: RegistrationStatus,
  paymentStatus: PaymentStatus | undefined,
  profileComplete: boolean,
): boolean {
  return getTicketBlockers(fee, registrationStatus, paymentStatus, profileComplete).length === 0
}

export function resolveEntryStamp(
  fee: number,
  registrationStatus: RegistrationStatus,
  paymentStatus: PaymentStatus | undefined,
  profileComplete: boolean,
): StampVariant {
  if (registrationStatus === 'CANCELLED') return 'cancelled'
  if (!profileComplete) return 'pending'
  if (fee > 0) {
    if (paymentStatus === 'COMPLETED') return 'paid'
    if (paymentStatus === 'FAILED') return 'failed'
    return 'pending'
  }
  if (registrationStatus === 'REGISTERED') return 'registered'
  return 'pending'
}

export function ticketStatusMessage(blockers: TicketBlocker[]): string | null {
  if (!blockers.length) return null
  if (blockers.includes('cancelled')) return 'This registration was cancelled.'
  const parts: string[] = []
  if (blockers.includes('payment')) parts.push('complete payment')
  if (blockers.includes('profile')) parts.push('verify email and phone on your profile')
  if (blockers.includes('registration')) parts.push('finish registration')
  return `To activate your pass, ${parts.join(' and ')}.`
}
