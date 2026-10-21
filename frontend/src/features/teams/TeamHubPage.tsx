import {
  deleteInvitation,
  deleteJoinRequest,
  deleteTeam,
  leaveTeam,
  getTeam,
  getTeamMembers,
  inviteToTeam,
  listTeamRequests,
  listMyTeamRequests,
  removeTeamMember,
  requestToJoin,
  respondToInvitation,
  respondToRequest,
  transferOwnership,
  updateTeam,
} from '@/api/teams'
import { Select } from '@/components/ui/Select'
import { Button } from '@/components/ui/Button'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { StatusBadge } from '@/components/StatusBadge'
import { useToast } from '@/components/ui/Toast'
import { useAuth } from '@/hooks/useAuth'
import {
  formatMembershipUser,
  isInvitation,
  isJoinRequest,
  membershipStatus,
} from '@/lib/team-membership'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'

export function TeamHubPage() {
  const { eventId: eRaw, teamId: tRaw } = useParams()
  const eventId = Number(eRaw)
  const teamId = Number(tRaw)
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const { toast } = useToast()
  const [inviteEmail, setInviteEmail] = useState('')
  const [teamNameEdit, setTeamNameEdit] = useState('')
  const [transferToUserId, setTransferToUserId] = useState('')

  const team = useQuery({
    queryKey: ['teams', eventId, teamId],
    queryFn: () => getTeam(eventId, teamId),
    enabled: Number.isFinite(eventId) && Number.isFinite(teamId),
  })

  const members = useQuery({
    queryKey: ['teams', eventId, teamId, 'members'],
    queryFn: () => getTeamMembers(eventId, teamId),
    enabled: Number.isFinite(eventId) && Number.isFinite(teamId),
  })

  const isOwner =
    team.isSuccess &&
    user?.id != null &&
    Number(team.data?.ownerId) === Number(user.id)

  const teamRequests = useQuery({
    queryKey: ['teams', eventId, teamId, 'requests'],
    queryFn: () => listTeamRequests(eventId, teamId),
    enabled: Number.isFinite(eventId) && Number.isFinite(teamId) && isOwner,
    refetchInterval: isOwner ? 8000 : false,
  })

  const myMembership = useQuery({
    queryKey: ['team-requests', 'me', user?.id],
    queryFn: listMyTeamRequests,
    enabled: Boolean(user?.id),
  })

  const isMember = members.data?.some((m) => m.id === user?.id) ?? false

  const transferCandidates = useMemo(
    () => (members.data ?? []).filter((m) => m.id !== team.data?.ownerId),
    [members.data, team.data?.ownerId],
  )

  const myPendingForTeam = myMembership.data?.find(
    (r) => r.teamId === teamId && membershipStatus(r) === 'PENDING',
  )
  const myInvitation =
    myPendingForTeam && isInvitation(myPendingForTeam) ? myPendingForTeam : undefined
  const myJoinRequest =
    myPendingForTeam && isJoinRequest(myPendingForTeam) ? myPendingForTeam : undefined

  const joinRequests =
    teamRequests.data?.filter((r) => isJoinRequest(r) && membershipStatus(r) === 'PENDING') ?? []
  const sentInvitations =
    teamRequests.data?.filter((r) => isInvitation(r) && membershipStatus(r) === 'PENDING') ?? []

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['teams', eventId, teamId] })
    void queryClient.invalidateQueries({ queryKey: ['team-requests', 'me'] })
  }

  const invite = useMutation({
    mutationFn: () => inviteToTeam(eventId, teamId, { email: inviteEmail.trim() }),
    onSuccess: () => {
      setInviteEmail('')
      invalidate()
      toast('Invitation sent')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const respondJoin = useMutation({
    mutationFn: ({ userId, status }: { userId: number; status: 'APPROVED' | 'REJECTED' }) =>
      respondToRequest(eventId, teamId, userId, { status }),
    onSuccess: (_, { status }) => {
      invalidate()
      toast(status === 'APPROVED' ? 'Member added' : 'Join request rejected')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const cancelInvite = useMutation({
    mutationFn: (userId: number) => deleteInvitation(eventId, teamId, userId),
    onSuccess: () => {
      invalidate()
      toast('Invitation cancelled')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const join = useMutation({
    mutationFn: () => requestToJoin(eventId, teamId),
    onSuccess: () => {
      invalidate()
      toast('Join request sent to team owner')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const cancelJoin = useMutation({
    mutationFn: () => deleteJoinRequest(eventId, teamId),
    onSuccess: () => {
      invalidate()
      toast('Join request withdrawn')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const respondInvite = useMutation({
    mutationFn: (status: 'APPROVED' | 'REJECTED') =>
      respondToInvitation(eventId, teamId, { status }),
    onSuccess: (_, status) => {
      invalidate()
      void queryClient.invalidateQueries({ queryKey: ['teams', 'me'] })
      toast(status === 'APPROVED' ? 'You joined the team' : 'Invitation declined')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const removeMember = useMutation({
    mutationFn: (userId: number) => removeTeamMember(eventId, teamId, userId),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['teams', eventId, teamId, 'members'] }),
  })

  const renameTeam = useMutation({
    mutationFn: () => updateTeam(eventId, teamId, { name: teamNameEdit.trim() }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['teams', eventId, teamId] })
      void queryClient.invalidateQueries({ queryKey: ['teams', 'me'] })
      toast('Team renamed')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const transfer = useMutation({
    mutationFn: () => transferOwnership(eventId, teamId, Number(transferToUserId)),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['teams', eventId, teamId] })
      void queryClient.invalidateQueries({ queryKey: ['teams', 'me'] })
      setTransferToUserId('')
      toast('Ownership transferred')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const removeTeam = useMutation({
    mutationFn: () => deleteTeam(eventId, teamId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['teams', 'me'] })
      toast('Team deleted')
      navigate('/teams')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const leave = useMutation({
    mutationFn: () => leaveTeam(eventId, teamId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['teams', 'me'] })
      toast('You left the team')
      navigate('/teams')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  useEffect(() => {
    if (team.data?.name) setTeamNameEdit(team.data.name)
  }, [team.data?.name])

  if (!Number.isFinite(eventId) || !Number.isFinite(teamId)) {
    return <p className="text-signal">Invalid team link.</p>
  }
  if (team.isError) {
    return <p className="text-signal">Could not load this team.</p>
  }

  if (team.isLoading || !team.data) {
    return <p className="text-ink-muted">Loading team…</p>
  }

  return (
    <div className="space-y-8 max-w-xl">
      <div>
        <Link to="/teams" className="text-sm text-stamp underline">← My teams</Link>
        <h1 className="text-3xl font-bold mt-2">{team.data.name}</h1>
        <p className="text-sm text-ink-muted">
          Event #{eventId}
          {isOwner ? ' · You are the owner' : isMember ? ' · Member' : ''}
        </p>
      </div>

      <section>
        <h2 className="font-display text-lg mb-2">Members ({members.data?.length ?? 0})</h2>
        <ul className="space-y-1 text-sm">
          {members.data?.map((m) => (
            <li key={m.id} className="flex justify-between rounded border border-border px-3 py-2">
              <span>
                {m.name} ({m.email})
                {m.id === team.data.ownerId ? ' · owner' : ''}
              </span>
              {isOwner && m.id !== user?.id ? (
                <button
                  type="button"
                  className="text-signal text-xs"
                  onClick={() => removeMember.mutate(m.id)}
                >
                  Remove
                </button>
              ) : null}
            </li>
          ))}
        </ul>
      </section>

      {myInvitation ? (
        <section className="space-y-3 rounded-lg border border-stamp/40 bg-surface p-4">
          <h2 className="font-display text-lg">You are invited</h2>
          <p className="text-sm text-ink-muted">Accept to join this team before registering for the event.</p>
          <div className="flex gap-2">
            <Button type="button" onClick={() => respondInvite.mutate('APPROVED')}>
              Accept invitation
            </Button>
            <Button type="button" variant="ghost" onClick={() => respondInvite.mutate('REJECTED')}>
              Decline
            </Button>
          </div>
        </section>
      ) : null}

      {!isOwner && !isMember && !myInvitation ? (
        <section className="space-y-3 rounded-lg border border-border p-4">
          <h2 className="font-display text-lg">Join this team</h2>
          {myJoinRequest ? (
            <>
              <p className="text-sm text-ink-muted">Your request is waiting for the owner to approve.</p>
              <Button type="button" variant="secondary" onClick={() => cancelJoin.mutate()}>
                Withdraw request
              </Button>
            </>
          ) : (
            <>
              <p className="text-sm text-ink-muted">
                Send a request to the team owner. You can also ask them to invite you by email.
              </p>
              <Button type="button" onClick={() => join.mutate()}>Request to join</Button>
            </>
          )}
        </section>
      ) : null}

      {isOwner ? (
        <>
          <section className="space-y-3">
            <h2 className="font-display text-lg">Join requests</h2>
            {teamRequests.isError ? (
              <p className="text-sm text-signal">
                Could not load requests: {(teamRequests.error as Error).message}
              </p>
            ) : null}
            {joinRequests.length === 0 ? (
              <p className="text-sm text-ink-muted">No pending join requests.</p>
            ) : (
              joinRequests.map((r) => (
                <div
                  key={r.id}
                  className="flex flex-wrap items-center justify-between gap-2 rounded border border-border p-3 text-sm"
                >
                  <span>{formatMembershipUser(r)}</span>
                  <div className="flex gap-2 items-center">
                    <StatusBadge status={membershipStatus(r)} />
                    <Button
                      type="button"
                      variant="secondary"
                      onClick={() => respondJoin.mutate({ userId: r.userId, status: 'APPROVED' })}
                    >
                      Approve
                    </Button>
                    <Button
                      type="button"
                      variant="ghost"
                      onClick={() => respondJoin.mutate({ userId: r.userId, status: 'REJECTED' })}
                    >
                      Reject
                    </Button>
                  </div>
                </div>
              ))
            )}
          </section>

          <section className="space-y-3">
            <h2 className="font-display text-lg">Sent invitations</h2>
            {sentInvitations.length === 0 ? (
              <p className="text-sm text-ink-muted">No pending invitations.</p>
            ) : (
              sentInvitations.map((r) => (
                <div
                  key={r.id}
                  className="flex items-center justify-between rounded border border-border p-3 text-sm"
                >
                  <span>{formatMembershipUser(r)}</span>
                  <Button type="button" variant="ghost" onClick={() => cancelInvite.mutate(r.userId)}>
                    Cancel
                  </Button>
                </div>
              ))
            )}
          </section>

          <section className="space-y-3">
            <h2 className="font-display text-lg">Invite by email</h2>
            <p className="text-sm text-ink-muted">
              The user must already have an account with a verified email and phone.
            </p>
            <Field label="Email">
              <Input
                type="email"
                value={inviteEmail}
                onChange={(e) => setInviteEmail(e.target.value)}
                placeholder="teammate@example.com"
              />
            </Field>
            <Button
              type="button"
              disabled={!inviteEmail.trim() || invite.isPending}
              onClick={() => invite.mutate()}
            >
              Send invitation
            </Button>
          </section>

          <section className="space-y-3 rounded-lg border border-border p-4">
            <h2 className="font-display text-lg">Team settings</h2>
            <Field label="Team name">
              <Input value={teamNameEdit} onChange={(e) => setTeamNameEdit(e.target.value)} />
            </Field>
            <Button
              type="button"
              variant="secondary"
              disabled={!teamNameEdit.trim() || teamNameEdit === team.data.name}
              onClick={() => renameTeam.mutate()}
            >
              Save name
            </Button>
            <Field label="Transfer ownership to">
              <Select
                value={transferToUserId}
                onValueChange={setTransferToUserId}
                placeholder="Select member…"
                options={transferCandidates.map((m) => ({
                  value: String(m.id),
                  label: `${m.name} (${m.email})`,
                }))}
              />
            </Field>
            <Button
              type="button"
              variant="secondary"
              disabled={!transferToUserId || transfer.isPending}
              onClick={() => transfer.mutate()}
            >
              Transfer ownership
            </Button>
            <p className="text-xs text-ink-muted">
              Deleting a team removes members and pending requests. You cannot delete after the team
              has completed event registration.
            </p>
            <Button
              type="button"
              variant="ghost"
              className="text-signal"
              disabled={removeTeam.isPending}
              onClick={() => {
                if (window.confirm(`Delete team "${team.data.name}"? This cannot be undone.`)) {
                  removeTeam.mutate()
                }
              }}
            >
              Delete team
            </Button>
          </section>
        </>
      ) : null}

      {isMember && !isOwner ? (
        <section className="space-y-2 rounded-lg border border-border p-4">
          <h2 className="font-display text-lg">Leave team</h2>
          <p className="text-sm text-ink-muted">
            You can rejoin later with a new request or invitation, unless the team has already
            completed event registration.
          </p>
          <Button
            type="button"
            variant="ghost"
            className="text-signal"
            disabled={leave.isPending}
            onClick={() => {
              if (window.confirm(`Leave team "${team.data.name}"?`)) leave.mutate()
            }}
          >
            Leave this team
          </Button>
        </section>
      ) : null}

      {isMember || isOwner ? (
        <Link to={`/register/${eventId}`}>
          <Button type="button" className="w-full">Register for this event with this team</Button>
        </Link>
      ) : null}
    </div>
  )
}
