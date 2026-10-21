import { ApiError, apiRequest, isMockMode } from './client'
import type {
  CreateEventRequest,
  EventResponse,
  MessageResponse,
  UpdateEventRequest,
} from './types'

const mockEvents: EventResponse[] = [
  {
    id: 1,
    name: 'Mock Hackathon',
    description: 'Build something bold in 24 hours.',
    createdBy: 2,
    type: 'TEAM',
    visibility: 'PUBLIC',
    status: 'PUBLISHED',
    maxParticipants: 200,
    minTeamSize: 2,
    maxTeamSize: 4,
    registrationFee: 499,
    registrationDeadline: new Date(Date.now() + 86400000 * 14).toISOString(),
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
]

export async function listVisibleEvents(): Promise<EventResponse[]> {
  if (isMockMode()) return mockEvents
  return apiRequest<EventResponse[]>('/api/events', { auth: false })
}

export async function getEvent(eventId: number): Promise<EventResponse> {
  if (isMockMode()) {
    const e = mockEvents.find((x) => x.id === eventId)
    if (!e) throw new Error('Not found')
    return e
  }
  return apiRequest<EventResponse>(`/api/events/${eventId}`, { auth: false })
}

export async function listMyEvents(): Promise<EventResponse[]> {
  if (isMockMode()) return mockEvents
  return apiRequest<EventResponse[]>('/api/events/me')
}

/** Load an event the current moderator owns (includes draft / non-public). */
export async function getManagedEvent(eventId: number): Promise<EventResponse> {
  if (isMockMode()) {
    const e = mockEvents.find((x) => x.id === eventId)
    if (!e) throw new Error('Not found')
    return e
  }
  const mine = await listMyEvents()
  const event = mine.find((e) => e.id === eventId)
  if (!event) {
    throw new ApiError({
      timestamp: new Date().toISOString(),
      status: 404,
      error: 'Not Found',
      code: 'NOT_FOUND',
      message: 'Event not found',
      path: `/api/events/me`,
    })
  }
  return event
}

export async function createEvent(body: CreateEventRequest): Promise<EventResponse> {
  if (isMockMode()) {
    const base = mockEvents[0]!
    return {
      ...base,
      name: body.name,
      description: body.description,
      type: body.type,
      visibility: body.visibility ?? base.visibility,
      maxParticipants: body.maxParticipants,
      minTeamSize: body.minTeamSize ?? base.minTeamSize,
      maxTeamSize: body.maxTeamSize ?? base.maxTeamSize,
      registrationFee: body.registrationFee ?? base.registrationFee,
      registrationDeadline: body.registrationDeadline,
      id: 99,
      createdBy: 1,
      status: 'DRAFT',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }
  }
  return apiRequest<EventResponse>('/api/events', { method: 'POST', body: JSON.stringify(body) })
}

export async function updateEvent(eventId: number, body: UpdateEventRequest): Promise<EventResponse> {
  if (isMockMode()) return { ...mockEvents[0], id: eventId, ...body, updatedAt: new Date().toISOString() } as EventResponse
  return apiRequest<EventResponse>(`/api/events/${eventId}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function deleteEvent(eventId: number): Promise<MessageResponse> {
  if (isMockMode()) return { message: 'Event deleted successfully' }
  return apiRequest<MessageResponse>(`/api/events/${eventId}`, { method: 'DELETE' })
}

function mockWithStatus(eventId: number, status: EventResponse['status']): EventResponse {
  return { ...mockEvents[0]!, id: eventId, status }
}

export async function publishEvent(eventId: number): Promise<EventResponse> {
  if (isMockMode()) return mockWithStatus(eventId, 'PUBLISHED')
  return apiRequest<EventResponse>(`/api/events/${eventId}/publish`, { method: 'PUT' })
}

export async function cancelEvent(eventId: number): Promise<EventResponse> {
  if (isMockMode()) return mockWithStatus(eventId, 'CANCELLED')
  return apiRequest<EventResponse>(`/api/events/${eventId}/cancel`, { method: 'PUT' })
}

export async function completeEvent(eventId: number): Promise<EventResponse> {
  if (isMockMode()) return mockWithStatus(eventId, 'COMPLETED')
  return apiRequest<EventResponse>(`/api/events/${eventId}/complete`, { method: 'PUT' })
}
