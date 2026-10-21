import { toIsoString } from '@/lib/iso-datetime'
import { apiRequest, isMockMode } from './client'
import type { EventRegistrationResponse, MessageResponse, RegisterEventRequest } from './types'

function normalizeRegistration(
  body: EventRegistrationResponse,
): EventRegistrationResponse {
  const registeredAt = toIsoString(body.registeredAt)
  return {
    ...body,
    registeredAt: registeredAt ?? new Date().toISOString(),
  }
}

export async function registerForEvent(
  eventId: number,
  body: RegisterEventRequest = {},
): Promise<EventRegistrationResponse> {
  if (isMockMode()) {
    return normalizeRegistration({
      id: 10,
      eventId,
      userId: 1,
      teamId: body.teamId ?? null,
      status: 'PENDING',
      registeredAt: new Date().toISOString(),
    })
  }
  const res = await apiRequest<EventRegistrationResponse>(`/api/events/${eventId}/registrations`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
  return normalizeRegistration(res)
}

export async function listMyRegistrations(): Promise<EventRegistrationResponse[]> {
  if (isMockMode()) return []
  const list = await apiRequest<EventRegistrationResponse[]>('/api/registrations/mine')
  return list.map(normalizeRegistration)
}

export async function cancelRegistration(
  eventId: number,
  registrationId: number,
): Promise<MessageResponse> {
  if (isMockMode()) return { message: 'Registration cancelled successfully' }
  return apiRequest<MessageResponse>(
    `/api/events/${eventId}/registrations/${registrationId}`,
    { method: 'DELETE' },
  )
}
