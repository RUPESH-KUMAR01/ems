import { apiRequest, isMockMode } from './client'
import type {
  AdminForceRegisterRequest,
  AdminMessageResponse,
  AdminResetPasswordRequest,
  AdminUpdateEventStatusRequest,
  AdminUpdatePaymentStatusRequest,
  AdminUpdateRegistrationStatusRequest,
  AdminUpdateVerificationRequest,
  ChangeUserRoleRequest,
  CreateEventRequest,
  CreateManagedUserRequest,
  EventRegistrationResponse,
  EventResponse,
  PaymentResponse,
  RespondToRequestRequest,
  SystemStatsResponse,
  TeamMembershipResponse,
  TeamResponse,
  UpdateEventRequest,
  UpdateTeamRequest,
  UpdateUserRequest,
  UserResponse,
} from './types'

const mockStats: SystemStatsResponse = {
  totalUsers: 42,
  usersByRole: { USER: 35, MODERATOR: 5, ADMIN: 2 },
  totalEvents: 8,
  eventsByStatus: { DRAFT: 2, PUBLISHED: 4, COMPLETED: 1, CANCELLED: 1 },
  totalTeams: 12,
  totalRegistrations: 56,
  registrationsByStatus: { PENDING: 4, REGISTERED: 50, CANCELLED: 2 },
  totalPayments: 30,
  paymentsByStatus: { PENDING: 2, COMPLETED: 26, FAILED: 2, CANCELLED: 0 },
  totalRevenue: 12500,
}

export async function adminGetStats(): Promise<SystemStatsResponse> {
  if (isMockMode()) return mockStats
  return apiRequest<SystemStatsResponse>('/api/admin/stats')
}

export async function adminListUsers(): Promise<UserResponse[]> {
  if (isMockMode()) return []
  return apiRequest<UserResponse[]>('/api/admin/users')
}

export async function adminCreateUser(body: CreateManagedUserRequest): Promise<UserResponse> {
  if (isMockMode()) {
    return {
      id: 99,
      email: body.email,
      name: body.name,
      role: body.role,
      emailVerified: false,
      phoneVerified: false,
    }
  }
  return apiRequest<UserResponse>('/api/admin/users', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function adminUpdateUser(userId: number, body: UpdateUserRequest): Promise<UserResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<UserResponse>(`/api/admin/users/${userId}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function adminGetUser(userId: number): Promise<UserResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<UserResponse>(`/api/admin/users/${userId}`)
}

export async function adminGetUserByEmail(email: string): Promise<UserResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<UserResponse>(`/api/admin/users/search/email?email=${encodeURIComponent(email)}`)
}

export async function adminGetUserByPhone(phone: string): Promise<UserResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<UserResponse>(`/api/admin/users/search/phone?phone=${encodeURIComponent(phone)}`)
}

export async function adminChangeRole(
  userId: number,
  body: ChangeUserRoleRequest,
): Promise<UserResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<UserResponse>(`/api/admin/users/${userId}/role`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function adminResetPassword(
  userId: number,
  body: AdminResetPasswordRequest,
): Promise<UserResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<UserResponse>(`/api/admin/users/${userId}/password`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function adminUpdateVerification(
  userId: number,
  body: AdminUpdateVerificationRequest,
): Promise<UserResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<UserResponse>(`/api/admin/users/${userId}/verification`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function adminDeleteUser(userId: number): Promise<AdminMessageResponse> {
  if (isMockMode()) return { message: 'User deleted' }
  return apiRequest<AdminMessageResponse>(`/api/admin/users/${userId}`, { method: 'DELETE' })
}

export async function adminGetUserTeams(userId: number): Promise<TeamResponse[]> {
  if (isMockMode()) return []
  return apiRequest<TeamResponse[]>(`/api/admin/users/${userId}/teams`)
}

export async function adminGetUserRegistrations(
  userId: number,
): Promise<EventRegistrationResponse[]> {
  if (isMockMode()) return []
  return apiRequest<EventRegistrationResponse[]>(`/api/admin/users/${userId}/registrations`)
}

export async function adminListTeams(): Promise<TeamResponse[]> {
  if (isMockMode()) return []
  return apiRequest<TeamResponse[]>('/api/admin/teams')
}

export async function adminGetTeam(teamId: number): Promise<TeamResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<TeamResponse>(`/api/admin/teams/${teamId}`)
}

export async function adminGetTeamMembers(teamId: number): Promise<UserResponse[]> {
  if (isMockMode()) return []
  return apiRequest<UserResponse[]>(`/api/admin/teams/${teamId}/members`)
}

export async function adminGetTeamRequests(teamId: number): Promise<TeamMembershipResponse[]> {
  if (isMockMode()) return []
  return apiRequest<TeamMembershipResponse[]>(`/api/admin/teams/${teamId}/requests`)
}

export async function adminUpdateTeam(
  teamId: number,
  body: UpdateTeamRequest,
): Promise<TeamResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<TeamResponse>(`/api/admin/teams/${teamId}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function adminTransferTeamOwnership(
  teamId: number,
  userId: number,
): Promise<TeamResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<TeamResponse>(`/api/admin/teams/${teamId}/owner/${userId}`, {
    method: 'PUT',
  })
}

export async function adminForceAddTeamMember(
  teamId: number,
  userId: number,
): Promise<TeamResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<TeamResponse>(`/api/admin/teams/${teamId}/members/${userId}`, {
    method: 'POST',
  })
}

export async function adminForceRespondTeamRequest(
  teamId: number,
  userId: number,
  body: RespondToRequestRequest,
): Promise<TeamMembershipResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<TeamMembershipResponse>(
    `/api/admin/teams/${teamId}/requests/${userId}`,
    { method: 'PUT', body: JSON.stringify(body) },
  )
}

export async function adminRemoveTeamMember(
  teamId: number,
  userId: number,
): Promise<AdminMessageResponse> {
  if (isMockMode()) return { message: 'removed' }
  return apiRequest<AdminMessageResponse>(
    `/api/admin/teams/${teamId}/members/${userId}`,
    { method: 'DELETE' },
  )
}

export async function adminDeleteTeam(teamId: number): Promise<AdminMessageResponse> {
  if (isMockMode()) return { message: 'deleted' }
  return apiRequest<AdminMessageResponse>(`/api/admin/teams/${teamId}`, { method: 'DELETE' })
}

export async function adminCreateEvent(body: CreateEventRequest): Promise<EventResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<EventResponse>('/api/admin/events', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function adminListEvents(): Promise<EventResponse[]> {
  if (isMockMode()) return []
  return apiRequest<EventResponse[]>('/api/admin/events')
}

export async function adminGetEvent(eventId: number): Promise<EventResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<EventResponse>(`/api/admin/events/${eventId}`)
}

export async function adminUpdateEvent(
  eventId: number,
  body: UpdateEventRequest,
): Promise<EventResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<EventResponse>(`/api/admin/events/${eventId}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function adminUpdateEventStatus(
  eventId: number,
  body: AdminUpdateEventStatusRequest,
): Promise<EventResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<EventResponse>(`/api/admin/events/${eventId}/status`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function adminDeleteEvent(eventId: number): Promise<AdminMessageResponse> {
  if (isMockMode()) return { message: 'deleted' }
  return apiRequest<AdminMessageResponse>(`/api/admin/events/${eventId}`, { method: 'DELETE' })
}

export async function adminGetEventTeams(eventId: number): Promise<TeamResponse[]> {
  if (isMockMode()) return []
  return apiRequest<TeamResponse[]>(`/api/admin/events/${eventId}/teams`)
}

export async function adminGetEventRegistrations(
  eventId: number,
): Promise<EventRegistrationResponse[]> {
  if (isMockMode()) return []
  return apiRequest<EventRegistrationResponse[]>(`/api/admin/events/${eventId}/registrations`)
}

export async function adminListRegistrations(): Promise<EventRegistrationResponse[]> {
  if (isMockMode()) return []
  return apiRequest<EventRegistrationResponse[]>('/api/admin/registrations')
}

export async function adminGetRegistration(
  registrationId: number,
): Promise<EventRegistrationResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<EventRegistrationResponse>(`/api/admin/registrations/${registrationId}`)
}

export async function adminForceRegister(
  body: AdminForceRegisterRequest,
): Promise<EventRegistrationResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<EventRegistrationResponse>('/api/admin/registrations', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function adminUpdateRegistrationStatus(
  registrationId: number,
  body: AdminUpdateRegistrationStatusRequest,
): Promise<EventRegistrationResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<EventRegistrationResponse>(
    `/api/admin/registrations/${registrationId}/status`,
    { method: 'PUT', body: JSON.stringify(body) },
  )
}

export async function adminDeleteRegistration(
  registrationId: number,
): Promise<AdminMessageResponse> {
  if (isMockMode()) return { message: 'deleted' }
  return apiRequest<AdminMessageResponse>(`/api/admin/registrations/${registrationId}`, {
    method: 'DELETE',
  })
}

export async function adminListPayments(): Promise<PaymentResponse[]> {
  if (isMockMode()) return []
  return apiRequest<PaymentResponse[]>('/api/admin/payments')
}

export async function adminGetPayment(paymentId: number): Promise<PaymentResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<PaymentResponse>(`/api/admin/payments/${paymentId}`)
}

export async function adminGetPaymentByOrder(orderId: string): Promise<PaymentResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<PaymentResponse>(`/api/admin/payments/order/${orderId}`)
}

export async function adminGetPaymentByRegistration(
  registrationId: number,
): Promise<PaymentResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<PaymentResponse>(`/api/admin/payments/registration/${registrationId}`)
}

export async function adminUpdatePaymentStatus(
  paymentId: number,
  body: AdminUpdatePaymentStatusRequest,
): Promise<PaymentResponse> {
  if (isMockMode()) throw new Error('mock')
  return apiRequest<PaymentResponse>(`/api/admin/payments/${paymentId}/status`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function adminDeletePayment(paymentId: number): Promise<AdminMessageResponse> {
  if (isMockMode()) return { message: 'deleted' }
  return apiRequest<AdminMessageResponse>(`/api/admin/payments/${paymentId}`, {
    method: 'DELETE',
  })
}
