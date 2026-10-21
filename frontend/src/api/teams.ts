import { apiRequest, isMockMode } from './client'
import type {
  CreateTeamRequest,
  InviteUserRequest,
  MessageResponse,
  RespondToRequestRequest,
  TeamMembershipResponse,
  TeamResponse,
  UpdateTeamRequest,
  UserResponse,
} from './types'

const mockTeam: TeamResponse = {
  id: 1,
  name: 'Mock Squad',
  ownerId: 1,
  eventId: 1,
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
}

export async function listTeamsForEvent(eventId: number): Promise<TeamResponse[]> {
  if (isMockMode()) return [mockTeam]
  return apiRequest<TeamResponse[]>(`/api/events/${eventId}/teams`)
}

export async function createTeam(eventId: number, body: CreateTeamRequest): Promise<TeamResponse> {
  if (isMockMode()) return { ...mockTeam, eventId, name: body.name }
  return apiRequest<TeamResponse>(`/api/events/${eventId}/teams`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function getTeam(eventId: number, teamId: number): Promise<TeamResponse> {
  if (isMockMode()) return { ...mockTeam, id: teamId, eventId }
  return apiRequest<TeamResponse>(`/api/events/${eventId}/teams/${teamId}`)
}

export async function updateTeam(
  eventId: number,
  teamId: number,
  body: UpdateTeamRequest,
): Promise<TeamResponse> {
  if (isMockMode()) return { ...mockTeam, id: teamId, eventId, ...body }
  return apiRequest<TeamResponse>(`/api/events/${eventId}/teams/${teamId}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function deleteTeam(eventId: number, teamId: number): Promise<MessageResponse> {
  if (isMockMode()) return { message: 'Team deleted successfully' }
  return apiRequest<MessageResponse>(`/api/events/${eventId}/teams/${teamId}`, {
    method: 'DELETE',
  })
}

export async function getTeamMembers(
  eventId: number,
  teamId: number,
): Promise<UserResponse[]> {
  if (isMockMode()) return []
  return apiRequest<UserResponse[]>(`/api/events/${eventId}/teams/${teamId}/members`)
}

export async function removeTeamMember(
  eventId: number,
  teamId: number,
  userId: number,
): Promise<MessageResponse> {
  if (isMockMode()) return { message: 'User removed from team successfully' }
  return apiRequest<MessageResponse>(
    `/api/events/${eventId}/teams/${teamId}/members/${userId}`,
    { method: 'DELETE' },
  )
}

export async function leaveTeam(eventId: number, teamId: number): Promise<MessageResponse> {
  if (isMockMode()) return { message: 'You left the team successfully' }
  return apiRequest<MessageResponse>(`/api/events/${eventId}/teams/${teamId}/leave`, {
    method: 'DELETE',
  })
}

export async function transferOwnership(
  eventId: number,
  teamId: number,
  userId: number,
): Promise<TeamResponse> {
  if (isMockMode()) return { ...mockTeam, ownerId: userId }
  return apiRequest<TeamResponse>(
    `/api/events/${eventId}/teams/${teamId}/owner/${userId}`,
    { method: 'PUT' },
  )
}

export async function requestToJoin(
  eventId: number,
  teamId: number,
): Promise<TeamMembershipResponse> {
  if (isMockMode()) {
    return {
      id: 1,
      teamId,
      userId: 2,
      type: 'PENDING',
      status: 'PENDING',
      requestType: 'JOIN_REQUEST',
    }
  }
  return apiRequest<TeamMembershipResponse>(
    `/api/events/${eventId}/teams/${teamId}/requests`,
    { method: 'POST' },
  )
}

export async function deleteJoinRequest(
  eventId: number,
  teamId: number,
): Promise<MessageResponse> {
  if (isMockMode()) return { message: 'Request deleted successfully' }
  return apiRequest<MessageResponse>(`/api/events/${eventId}/teams/${teamId}/requests`, {
    method: 'DELETE',
  })
}

export async function listTeamRequests(
  eventId: number,
  teamId: number,
): Promise<TeamMembershipResponse[]> {
  if (isMockMode()) return []
  return apiRequest<TeamMembershipResponse[]>(
    `/api/events/${eventId}/teams/${teamId}/requests`,
  )
}

export async function respondToRequest(
  eventId: number,
  teamId: number,
  userId: number,
  body: RespondToRequestRequest,
): Promise<TeamMembershipResponse> {
  if (isMockMode()) {
    return {
      id: 1,
      teamId,
      userId,
      type: body.status,
      status: body.status,
      requestType: 'JOIN_REQUEST',
    }
  }
  return apiRequest<TeamMembershipResponse>(
    `/api/events/${eventId}/teams/${teamId}/requests/${userId}`,
    { method: 'PUT', body: JSON.stringify(body) },
  )
}

export async function inviteToTeam(
  eventId: number,
  teamId: number,
  body: InviteUserRequest,
): Promise<TeamMembershipResponse> {
  if (isMockMode()) {
    return {
      id: 1,
      teamId,
      userId: 3,
      type: 'PENDING',
      status: 'PENDING',
      requestType: 'INVITATION',
    }
  }
  return apiRequest<TeamMembershipResponse>(
    `/api/events/${eventId}/teams/${teamId}/invitations`,
    { method: 'POST', body: JSON.stringify(body) },
  )
}

export async function deleteInvitation(
  eventId: number,
  teamId: number,
  userId: number,
): Promise<MessageResponse> {
  if (isMockMode()) return { message: 'Invitation deleted successfully' }
  return apiRequest<MessageResponse>(
    `/api/events/${eventId}/teams/${teamId}/invitations/${userId}`,
    { method: 'DELETE' },
  )
}

export async function respondToInvitation(
  eventId: number,
  teamId: number,
  body: RespondToRequestRequest,
): Promise<TeamMembershipResponse> {
  if (isMockMode()) {
    return {
      id: 1,
      teamId,
      userId: 1,
      type: body.status,
      status: body.status,
      requestType: 'INVITATION',
    }
  }
  return apiRequest<TeamMembershipResponse>(
    `/api/events/${eventId}/teams/${teamId}/invitations`,
    { method: 'PUT', body: JSON.stringify(body) },
  )
}

export async function listMyTeams(): Promise<TeamResponse[]> {
  if (isMockMode()) return [mockTeam]
  return apiRequest<TeamResponse[]>('/api/teams/me')
}

export async function listMyTeamRequests(): Promise<TeamMembershipResponse[]> {
  if (isMockMode()) return []
  return apiRequest<TeamMembershipResponse[]>('/api/team-requests/me')
}
