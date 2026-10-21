import type { Role } from '@/api/types'

const RANK: Record<Role, number> = {
  USER: 1,
  MODERATOR: 2,
  ADMIN: 3,
}

export function hasRole(userRole: Role | undefined, minimum: Role): boolean {
  if (!userRole) return false
  return RANK[userRole] >= RANK[minimum]
}

export function isAdminOnly(userRole: Role | undefined): boolean {
  return userRole === 'ADMIN'
}
