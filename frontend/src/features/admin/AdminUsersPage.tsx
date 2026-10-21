import {
  adminChangeRole,
  adminDeleteUser,
  adminListUsers,
  adminResetPassword,
  adminUpdateVerification,
} from '@/api/admin'
import type { Role, UserResponse } from '@/api/types'
import { DataTable } from '@/components/DataTable'
import { StatusBadge } from '@/components/StatusBadge'
import { Button } from '@/components/ui/Button'
import { ConfirmDialog } from '@/components/ui/Dialog'
import { Select } from '@/components/ui/Select'
import { useAuth } from '@/hooks/useAuth'
import { useToast } from '@/components/ui/Toast'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'

export function AdminUsersPage() {
  const { data = [], isLoading } = useQuery({ queryKey: ['admin', 'users'], queryFn: adminListUsers })
  const { user: me } = useAuth()
  const queryClient = useQueryClient()
  const { toast } = useToast()
  const [deleteId, setDeleteId] = useState<number | null>(null)
  const [verifyTarget, setVerifyTarget] = useState<UserResponse | null>(null)

  const changeRole = useMutation({
    mutationFn: ({ id, role }: { id: number; role: Role }) => adminChangeRole(id, { role }),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['admin', 'users'] }),
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const resetPw = useMutation({
    mutationFn: (id: number) => adminResetPassword(id, { newPassword: 'ResetPass123' }),
    onSuccess: () => toast('Password reset to ResetPass123'),
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const del = useMutation({
    mutationFn: (id: number) => adminDeleteUser(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      toast('User deleted')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const verifyBypass = useMutation({
    mutationFn: (user: UserResponse) =>
      adminUpdateVerification(user.id, {
        emailVerified: true,
        phoneVerified: true,
      }),
    onSuccess: (updated) => {
      void queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      toast(`Verification bypass applied for ${updated.email}`)
      setVerifyTarget(null)
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const verifyEmailOnly = useMutation({
    mutationFn: (user: UserResponse) =>
      adminUpdateVerification(user.id, {
        emailVerified: true,
        phoneVerified: user.phoneVerified,
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      toast('Email marked verified')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const verifyPhoneOnly = useMutation({
    mutationFn: (user: UserResponse) =>
      adminUpdateVerification(user.id, {
        emailVerified: user.emailVerified,
        phoneVerified: true,
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      toast('Phone marked verified')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  if (isLoading) return <p className="text-ink-muted">Loading users…</p>

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Users</h1>
      <DataTable
        rows={data}
        keyFn={(u) => u.id}
        columns={[
          { header: 'Name', cell: (u: UserResponse) => u.name },
          { header: 'Email', cell: (u) => u.email },
          { header: 'Role', cell: (u) => <StatusBadge status={u.role} /> },
          {
            header: 'Verification',
            cell: (u) => (
              <div className="flex flex-col gap-1 text-xs">
                <span>
                  Email:{' '}
                  <StatusBadge status={u.emailVerified ? 'APPROVED' : 'PENDING'} />
                </span>
                <span>
                  Phone:{' '}
                  <StatusBadge status={u.phoneVerified ? 'APPROVED' : 'PENDING'} />
                </span>
              </div>
            ),
          },
          {
            header: 'Actions',
            cell: (u) => (
              <div className="flex flex-col gap-2 min-w-[10rem]">
                <Select
                  value={u.role}
                  onValueChange={(role) => {
                    if (u.id === me?.id && role !== 'ADMIN') {
                      toast('Cannot demote yourself', 'error')
                      return
                    }
                    changeRole.mutate({ id: u.id, role: role as Role })
                  }}
                  options={[
                    { value: 'USER', label: 'USER' },
                    { value: 'MODERATOR', label: 'MODERATOR' },
                    { value: 'ADMIN', label: 'ADMIN' },
                  ]}
                />
                {!u.emailVerified ? (
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() => verifyEmailOnly.mutate(u)}
                  >
                    Mark email verified
                  </Button>
                ) : null}
                {!u.phoneVerified ? (
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() => verifyPhoneOnly.mutate(u)}
                  >
                    Mark phone verified
                  </Button>
                ) : null}
                {(!u.emailVerified || !u.phoneVerified) ? (
                  <Button type="button" variant="secondary" onClick={() => setVerifyTarget(u)}>
                    Bypass verification (both)
                  </Button>
                ) : null}
                <Button type="button" variant="ghost" onClick={() => resetPw.mutate(u.id)}>Reset password</Button>
                <Button
                  type="button"
                  variant="danger"
                  disabled={u.id === me?.id}
                  onClick={() => setDeleteId(u.id)}
                >
                  Delete
                </Button>
              </div>
            ),
          },
        ]}
      />
      <ConfirmDialog
        open={deleteId !== null}
        onOpenChange={() => setDeleteId(null)}
        title="Delete user?"
        description="Permanent deletion."
        destructive
        onConfirm={() => {
          if (deleteId) del.mutate(deleteId)
        }}
      />
      <ConfirmDialog
        open={verifyTarget !== null}
        onOpenChange={(open) => {
          if (!open) setVerifyTarget(null)
        }}
        title="Bypass verification?"
        description={
          verifyTarget
            ? `Mark ${verifyTarget.name} (${verifyTarget.email}) as email and phone verified without OTP. They can register and confirm tickets immediately.`
            : ''
        }
        confirmLabel="Mark verified"
        onConfirm={() => {
          if (verifyTarget) verifyBypass.mutate(verifyTarget)
        }}
      />
    </div>
  )
}
