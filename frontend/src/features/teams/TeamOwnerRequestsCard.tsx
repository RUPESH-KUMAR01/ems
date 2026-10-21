import {
  listTeamRequests,
  respondToRequest,
} from '@/api/teams'
import type { TeamResponse } from '@/api/types'
import { StatusBadge } from '@/components/StatusBadge'
import { Button } from '@/components/ui/Button'
import { useToast } from '@/components/ui/Toast'
import { formatMembershipUser, isJoinRequest, membershipStatus } from '@/lib/team-membership'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'

export function TeamOwnerRequestsCard({ team, ownerId }: { team: TeamResponse; ownerId: number }) {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  const requests = useQuery({
    queryKey: ['teams', team.eventId, team.id, 'requests'],
    queryFn: () => listTeamRequests(team.eventId, team.id),
    enabled: Number(team.ownerId) === Number(ownerId),
    refetchInterval: 8000,
  })

  const respond = useMutation({
    mutationFn: ({ userId, status }: { userId: number; status: 'APPROVED' | 'REJECTED' }) =>
      respondToRequest(team.eventId, team.id, userId, { status }),
    onSuccess: (_, { status }) => {
      void queryClient.invalidateQueries({ queryKey: ['teams', team.eventId, team.id, 'requests'] })
      void queryClient.invalidateQueries({ queryKey: ['teams', team.eventId, team.id] })
      void queryClient.invalidateQueries({ queryKey: ['teams', 'me'] })
      toast(status === 'APPROVED' ? 'Member added' : 'Join request rejected')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const joinRequests =
    requests.data?.filter((r) => isJoinRequest(r) && membershipStatus(r) === 'PENDING') ?? []

  return (
    <li className="rounded border border-border p-3 space-y-2">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div>
          <Link
            to={`/events/${team.eventId}/teams/${team.id}`}
            className="font-medium hover:underline"
          >
            {team.name}
          </Link>
          <p className="text-xs text-ink-muted">Event #{team.eventId} · You own this team</p>
        </div>
        <Link
          to={`/events/${team.eventId}/teams/${team.id}`}
          className="text-xs text-stamp underline"
        >
          Manage team
        </Link>
      </div>

      {requests.isError ? (
        <p className="text-sm text-signal">Could not load join requests. Open the team page to retry.</p>
      ) : requests.isLoading ? (
        <p className="text-xs text-ink-muted">Loading requests…</p>
      ) : joinRequests.length === 0 ? (
        <p className="text-xs text-ink-muted">No pending join requests.</p>
      ) : (
        <ul className="space-y-2 border-t border-border pt-2">
          {joinRequests.map((r) => (
            <li
              key={r.id}
              className="flex flex-wrap items-center justify-between gap-2 text-sm"
            >
              <span>{formatMembershipUser(r)} wants to join</span>
              <div className="flex gap-2 items-center">
                <StatusBadge status={membershipStatus(r)} />
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => respond.mutate({ userId: r.userId, status: 'APPROVED' })}
                >
                  Approve
                </Button>
                <Button
                  type="button"
                  variant="ghost"
                  onClick={() => respond.mutate({ userId: r.userId, status: 'REJECTED' })}
                >
                  Reject
                </Button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </li>
  )
}
