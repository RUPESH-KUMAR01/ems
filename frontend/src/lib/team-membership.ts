import type { TeamMembershipRequestType, TeamMembershipResponse, TeamMembershipStatus } from '@/api/types'

export function membershipStatus(r: TeamMembershipResponse): TeamMembershipStatus {
  if (r.status) return r.status
  const t = r.type
  if (t === 'PENDING' || t === 'APPROVED' || t === 'REJECTED') return t
  return 'PENDING'
}

export function membershipRequestType(r: TeamMembershipResponse): TeamMembershipRequestType {
  if (r.requestType) return r.requestType
  return 'JOIN_REQUEST'
}

export function isJoinRequest(r: TeamMembershipResponse): boolean {
  return membershipRequestType(r) === 'JOIN_REQUEST'
}

/** True when the user still has an outstanding join request awaiting owner action. */
export function hasPendingJoinRequest(
  requests: TeamMembershipResponse[] | undefined,
  teamId: number,
): boolean {
  return (
    requests?.some(
      (r) =>
        r.teamId === teamId &&
        isJoinRequest(r) &&
        membershipStatus(r) === 'PENDING',
    ) ?? false
  )
}

export function isInvitation(r: TeamMembershipResponse): boolean {
  return membershipRequestType(r) === 'INVITATION'
}

export function formatMembershipUser(r: TeamMembershipResponse): string {
  if (r.userName && r.userEmail) return `${r.userName} (${r.userEmail})`
  if (r.userName) return r.userName
  if (r.userEmail) return r.userEmail
  return `User #${r.userId}`
}
