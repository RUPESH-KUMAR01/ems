import { listVisibleEvents } from '@/api/events'
import {
  createTeam,
  listMyTeamRequests,
  listMyTeams,
  listTeamsForEvent,
  requestToJoin,
  respondToInvitation,
  deleteJoinRequest,
  leaveTeam,
} from '@/api/teams'
import { StatusBadge } from '@/components/StatusBadge'
import { Button } from '@/components/ui/Button'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { useToast } from '@/components/ui/Toast'
import { isRegistrationOpen } from '@/lib/event-lifecycle'
import {
  hasPendingJoinRequest,
  isInvitation,
  membershipStatus,
} from '@/lib/team-membership'
import { TeamOwnerRequestsCard } from '@/features/teams/TeamOwnerRequestsCard'
import { useAuth } from '@/hooks/useAuth'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'

export function TeamsPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const queryClient = useQueryClient()
  const { toast } = useToast()
  const { user } = useAuth()
  const preselectedEventId = Number(searchParams.get('eventId'))

  const teams = useQuery({ queryKey: ['teams', 'me'], queryFn: listMyTeams })
  const requests = useQuery({ queryKey: ['team-requests', 'me'], queryFn: listMyTeamRequests })
  const eventsQuery = useQuery({
    queryKey: ['events', 'visible'],
    queryFn: listVisibleEvents,
  })

  const teamEvents = useMemo(
    () =>
      (eventsQuery.data ?? []).filter(
        (e) => e.type === 'TEAM' && e.status === 'PUBLISHED' && isRegistrationOpen(e),
      ),
    [eventsQuery.data],
  )

  const [eventId, setEventId] = useState<number | ''>(
    Number.isFinite(preselectedEventId) ? preselectedEventId : '',
  )
  const [teamName, setTeamName] = useState('')
  const [browseEventId, setBrowseEventId] = useState<number | ''>(
    Number.isFinite(preselectedEventId) ? preselectedEventId : '',
  )

  const browseTeams = useQuery({
    queryKey: ['teams', 'event', browseEventId],
    queryFn: () => listTeamsForEvent(Number(browseEventId)),
    enabled: browseEventId !== '' && Number.isFinite(Number(browseEventId)),
  })

  const alreadyOnTeamForSelectedEvent = useMemo(() => {
    if (eventId === '') return false
    const eid = Number(eventId)
    return (teams.data ?? []).some((t) => t.eventId === eid)
  }, [teams.data, eventId])

  const createMutation = useMutation({
    mutationFn: async () => {
      const eid = Number(eventId)
      if (!Number.isFinite(eid)) throw new Error('Select an event')
      if (!teamName.trim()) throw new Error('Enter a team name')
      if ((teams.data ?? []).some((t) => t.eventId === eid)) {
        throw new Error(
          'You are already on a team for this event. Leave that team before creating another.',
        )
      }
      return createTeam(eid, { name: teamName.trim() })
    },
    onSuccess: (team) => {
      void queryClient.invalidateQueries({ queryKey: ['teams'] })
      setTeamName('')
      navigate(`/events/${team.eventId}/teams/${team.id}`)
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const joinMutation = useMutation({
    mutationFn: ({ eid, tid }: { eid: number; tid: number }) => requestToJoin(eid, tid),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['team-requests', 'me'] })
      toast('Join request sent')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const respondInviteMutation = useMutation({
    mutationFn: ({
      eid,
      tid,
      status,
    }: {
      eid: number
      tid: number
      status: 'APPROVED' | 'REJECTED'
    }) => respondToInvitation(eid, tid, { status }),
    onSuccess: (_, { status }) => {
      void queryClient.invalidateQueries({ queryKey: ['team-requests', 'me'] })
      void queryClient.invalidateQueries({ queryKey: ['teams', 'me'] })
      toast(status === 'APPROVED' ? 'Joined team' : 'Invitation declined')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const withdrawJoinMutation = useMutation({
    mutationFn: ({ eid, tid }: { eid: number; tid: number }) => deleteJoinRequest(eid, tid),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['team-requests', 'me'] })
      toast('Request withdrawn')
    },
  })

  const leaveTeamMutation = useMutation({
    mutationFn: ({ eventId: eid, teamId: tid }: { eventId: number; teamId: number }) =>
      leaveTeam(eid, tid),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['teams', 'me'] })
      toast('You left the team')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const myTeamsByTeamId = useMemo(() => new Set(teams.data?.map((t) => t.id) ?? []), [teams.data])

  const ownedTeams = useMemo(
    () =>
      (teams.data ?? []).filter(
        (t) => user?.id != null && Number(t.ownerId) === Number(user.id),
      ),
    [teams.data, user?.id],
  )

  return (
    <div className="account-page space-y-8">
      <div>
        <h1 className="text-3xl font-bold">My teams</h1>
        <p className="mt-1 text-sm text-ink-muted">
          Teams belong to a specific event. Create one here or during{' '}
          <span className="text-ink">team event</span> registration.
        </p>
      </div>

      <section className="space-y-4 rounded-lg border border-border bg-surface p-5">
        <h2 className="font-display text-lg">Create a team</h2>
        {eventsQuery.isLoading ? (
          <p className="text-sm text-ink-muted">Loading events…</p>
        ) : teamEvents.length === 0 ? (
          <p className="text-sm text-ink-muted">
            No open <strong>team</strong> events right now. Solo events do not use teams — publish a
            team event under Manage (moderator) or browse{' '}
            <Link to="/events" className="text-stamp underline">events</Link>.
          </p>
        ) : (
          <div className="space-y-4 max-w-md">
            <Field label="Team event">
              <Select
                value={eventId === '' ? '' : String(eventId)}
                onValueChange={(v) => setEventId(v ? Number(v) : '')}
                placeholder="Select event…"
                options={teamEvents.map((ev) => ({
                  value: String(ev.id),
                  label: ev.name,
                }))}
              />
            </Field>
            <Field label="Team name">
              <Input
                value={teamName}
                onChange={(e) => setTeamName(e.target.value)}
                placeholder="e.g. Code Crushers"
              />
            </Field>
            {alreadyOnTeamForSelectedEvent ? (
              <p className="text-sm text-ink-muted">
                You are already on a team for this event. Open it under{' '}
                <span className="text-ink">Your teams</span> below, or leave it before creating a
                new one.
              </p>
            ) : null}
            <Button
              type="button"
              disabled={
                createMutation.isPending ||
                eventId === '' ||
                !teamName.trim() ||
                alreadyOnTeamForSelectedEvent
              }
              onClick={() => createMutation.mutate()}
            >
              Create team
            </Button>
            {createMutation.error ? (
              <p className="text-sm text-signal">{(createMutation.error as Error).message}</p>
            ) : null}
            {eventId !== '' ? (
              <p className="text-xs text-ink-muted">
                After creating, invite members from the team page, then{' '}
                <Link to={`/register/${eventId}`} className="text-stamp underline">
                  register for the event
                </Link>{' '}
                with this team.
              </p>
            ) : null}
          </div>
        )}
      </section>

      <section className="space-y-4 rounded-lg border border-border bg-surface p-5">
        <h2 className="font-display text-lg">Find a team to join</h2>
        <p className="text-sm text-ink-muted">
          Browse teams for an event, send a join request, or open a team to accept an invitation.
        </p>
        {teamEvents.length > 0 ? (
          <div className="space-y-4 max-w-md">
            <Field label="Team event">
              <Select
                value={browseEventId === '' ? '' : String(browseEventId)}
                onValueChange={(v) => setBrowseEventId(v ? Number(v) : '')}
                placeholder="Select event…"
                options={teamEvents.map((ev) => ({
                  value: String(ev.id),
                  label: ev.name,
                }))}
              />
            </Field>
            {browseEventId !== '' ? (
              browseTeams.isLoading ? (
                <p className="text-sm text-ink-muted">Loading teams…</p>
              ) : browseTeams.data?.length ? (
                <ul className="space-y-2">
                  {browseTeams.data.map((t) => {
                    const alreadyMember = myTeamsByTeamId.has(t.id)
                    const pendingJoin = hasPendingJoinRequest(requests.data, t.id)
                    const eid = Number(browseEventId)
                    return (
                      <li
                        key={t.id}
                        className="flex flex-wrap items-center justify-between gap-2 rounded border border-border p-3 text-sm"
                      >
                        <Link
                          to={`/events/${eid}/teams/${t.id}`}
                          className="font-medium hover:underline"
                        >
                          {t.name}
                        </Link>
                        <div className="flex gap-2">
                          {alreadyMember ? (
                            <span className="text-xs text-mint">Member</span>
                          ) : pendingJoin ? (
                            <Button
                              type="button"
                              variant="ghost"
                              onClick={() => withdrawJoinMutation.mutate({ eid, tid: t.id })}
                            >
                              Withdraw request
                            </Button>
                          ) : (
                            <Button
                              type="button"
                              variant="secondary"
                              disabled={joinMutation.isPending}
                              onClick={() => joinMutation.mutate({ eid, tid: t.id })}
                            >
                              Request to join
                            </Button>
                          )}
                        </div>
                      </li>
                    )
                  })}
                </ul>
              ) : (
                <p className="text-sm text-ink-muted">No teams for this event yet — create one above.</p>
              )
            ) : null}
          </div>
        ) : null}
      </section>

      {ownedTeams.length > 0 && user?.id ? (
        <section className="space-y-3">
          <h2 className="font-display text-lg">Teams you manage</h2>
          <p className="text-sm text-ink-muted">
            Approve join requests here or open a team for invites, transfer ownership, and settings.
          </p>
          <ul className="space-y-3">
            {ownedTeams.map((t) => (
              <TeamOwnerRequestsCard key={t.id} team={t} ownerId={user.id} />
            ))}
          </ul>
        </section>
      ) : null}

      <section className="space-y-3">
        <h2 className="font-display text-lg">Your teams</h2>
        {teams.data?.length ? (
          <ul className="space-y-2">
            {teams.data.map((t) => {
              const isOwner =
                user?.id != null && Number(t.ownerId) === Number(user.id)
              return (
                <li
                  key={t.id}
                  className="flex flex-wrap items-center justify-between gap-2 rounded border border-border p-3"
                >
                  <div>
                    <Link
                      to={`/events/${t.eventId}/teams/${t.id}`}
                      className="font-medium hover:underline"
                    >
                      {t.name}
                    </Link>
                    <p className="text-xs text-ink-muted">
                      Event #{t.eventId}
                      {isOwner ? ' · Owner' : ' · Member'}
                    </p>
                  </div>
                  {!isOwner ? (
                    <Button
                      type="button"
                      variant="ghost"
                      className="text-signal text-sm"
                      disabled={leaveTeamMutation.isPending}
                      onClick={() => {
                        if (window.confirm(`Leave team "${t.name}"?`)) {
                          leaveTeamMutation.mutate({ eventId: t.eventId, teamId: t.id })
                        }
                      }}
                    >
                      Leave
                    </Button>
                  ) : null}
                </li>
              )
            })}
          </ul>
        ) : (
          <p className="text-sm text-ink-muted">No teams yet.</p>
        )}
      </section>

      <section className="space-y-3">
        <h2 className="font-display text-lg">Inbox — invitations & your requests</h2>
        {requests.data?.length ? (
          <ul className="space-y-2">
            {requests.data.map((r) => {
              const teamMeta = teams.data?.find((t) => t.id === r.teamId)
              const eventIdForLink = r.eventId ?? teamMeta?.eventId
              const label = teamMeta
                ? `${teamMeta.name} (event #${teamMeta.eventId})`
                : r.eventId
                  ? `Team #${r.teamId} (event #${r.eventId})`
                  : `Team #${r.teamId}`
              return (
                <li
                  key={r.id}
                  className="flex flex-wrap items-center justify-between gap-2 rounded border border-border p-3 text-sm"
                >
                  <div>
                    <p className="font-medium">{label}</p>
                    <p className="text-xs text-ink-muted">
                      {isInvitation(r) ? 'Invitation' : 'Your join request'}
                    </p>
                  </div>
                  <div className="flex gap-2 items-center">
                    <StatusBadge status={membershipStatus(r)} />
                    {eventIdForLink && isInvitation(r) ? (
                      <>
                        <Button
                          type="button"
                          variant="secondary"
                          onClick={() =>
                            respondInviteMutation.mutate({
                              eid: eventIdForLink,
                              tid: r.teamId,
                              status: 'APPROVED',
                            })
                          }
                        >
                          Accept
                        </Button>
                        <Button
                          type="button"
                          variant="ghost"
                          onClick={() =>
                            respondInviteMutation.mutate({
                              eid: eventIdForLink,
                              tid: r.teamId,
                              status: 'REJECTED',
                            })
                          }
                        >
                          Decline
                        </Button>
                      </>
                    ) : eventIdForLink ? (
                      <Link
                        to={`/events/${eventIdForLink}/teams/${r.teamId}`}
                        className="text-stamp underline text-xs"
                      >
                        View team
                      </Link>
                    ) : null}
                  </div>
                </li>
              )
            })}
          </ul>
        ) : (
          <p className="text-sm text-ink-muted">Nothing pending.</p>
        )}
      </section>
    </div>
  )
}
