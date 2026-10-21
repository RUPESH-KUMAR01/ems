import {
  adminGetEventRegistrations,
  adminListEvents,
  adminListPayments,
  adminListRegistrations,
  adminListTeams,
  adminUpdatePaymentStatus,
  adminUpdateRegistrationStatus,
} from '@/api/admin'
import { DataTable } from '@/components/DataTable'
import { StatusBadge } from '@/components/StatusBadge'
import { Button } from '@/components/ui/Button'
import { ConfirmDialog } from '@/components/ui/Dialog'
import { Select } from '@/components/ui/Select'
import type { PaymentStatus, RegistrationStatus } from '@/api/types'
import { useToast } from '@/components/ui/Toast'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'

export function AdminEventsPage() {
  const { data = [] } = useQuery({ queryKey: ['admin', 'events'], queryFn: adminListEvents })
  return (
    <div className="space-y-4">
      <h1 className="text-3xl font-bold">Events</h1>
      <DataTable
        rows={data}
        keyFn={(e) => e.id}
        columns={[
          { header: 'ID', cell: (e) => e.id },
          { header: 'Name', cell: (e) => e.name },
          { header: 'Status', cell: (e) => <StatusBadge status={e.status} /> },
        ]}
      />
    </div>
  )
}

export function AdminTeamsPage() {
  const { data = [] } = useQuery({ queryKey: ['admin', 'teams'], queryFn: adminListTeams })
  return (
    <div className="space-y-4">
      <h1 className="text-3xl font-bold">Teams</h1>
      <DataTable
        rows={data}
        keyFn={(t) => t.id}
        columns={[
          { header: 'ID', cell: (t) => t.id },
          { header: 'Name', cell: (t) => t.name },
          { header: 'Event', cell: (t) => t.eventId },
        ]}
      />
    </div>
  )
}

export function AdminRegistrationsPage() {
  const queryClient = useQueryClient()
  const { toast } = useToast()
  const { data = [] } = useQuery({
    queryKey: ['admin', 'registrations'],
    queryFn: adminListRegistrations,
  })

  const update = useMutation({
    mutationFn: ({ id, status }: { id: number; status: RegistrationStatus }) =>
      adminUpdateRegistrationStatus(id, { status }),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['admin', 'registrations'] }),
    onError: (e: Error) => toast(e.message, 'error'),
  })

  return (
    <div className="space-y-4">
      <h1 className="text-3xl font-bold">Registrations</h1>
      <DataTable
        rows={data}
        keyFn={(r) => r.id}
        columns={[
          { header: 'ID', cell: (r) => r.id },
          { header: 'Event', cell: (r) => r.eventId },
          { header: 'User', cell: (r) => r.userId },
          { header: 'Status', cell: (r) => <StatusBadge status={r.status} /> },
          {
            header: 'Override',
            cell: (r) => (
              <Select
                value={r.status}
                onValueChange={(v) => update.mutate({ id: r.id, status: v as RegistrationStatus })}
                options={[
                  { value: 'PENDING', label: 'PENDING' },
                  { value: 'REGISTERED', label: 'REGISTERED' },
                  { value: 'CANCELLED', label: 'CANCELLED' },
                ]}
              />
            ),
          },
        ]}
      />
    </div>
  )
}

export function AdminPaymentsPage() {
  const queryClient = useQueryClient()
  const { toast } = useToast()
  const [confirm, setConfirm] = useState<{ id: number; status: PaymentStatus } | null>(null)
  const { data = [] } = useQuery({ queryKey: ['admin', 'payments'], queryFn: adminListPayments })

  const update = useMutation({
    mutationFn: ({ id, status }: { id: number; status: PaymentStatus }) =>
      adminUpdatePaymentStatus(id, { status }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['admin', 'payments'] })
      toast('Payment status updated')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  return (
    <div className="space-y-4">
      <h1 className="text-3xl font-bold">Payments</h1>
      <DataTable
        rows={data}
        keyFn={(p) => p.id}
        columns={[
          { header: 'ID', cell: (p) => p.id },
          { header: 'Registration', cell: (p) => p.registrationId },
          { header: 'Amount', cell: (p) => p.amount },
          { header: 'Status', cell: (p) => <StatusBadge status={p.status} /> },
          {
            header: 'Override',
            cell: (p) => (
              <Button
                type="button"
                variant="ghost"
                onClick={() => setConfirm({ id: p.id, status: 'COMPLETED' })}
              >
                Mark completed
              </Button>
            ),
          },
        ]}
      />
      <ConfirmDialog
        open={confirm !== null}
        onOpenChange={() => setConfirm(null)}
        title="Override payment status?"
        description="Use only for support corrections."
        onConfirm={() => {
          if (confirm) update.mutate(confirm)
        }}
      />
    </div>
  )
}

export function AdminEventRegistrationsTool({ eventId }: { eventId: number }) {
  const { data = [] } = useQuery({
    queryKey: ['admin', 'events', eventId, 'registrations'],
    queryFn: () => adminGetEventRegistrations(eventId),
  })
  return (
    <DataTable
      rows={data}
      keyFn={(r) => r.id}
      columns={[
        { header: 'ID', cell: (r) => r.id },
        { header: 'User', cell: (r) => r.userId },
        { header: 'Status', cell: (r) => <StatusBadge status={r.status} /> },
      ]}
    />
  )
}
