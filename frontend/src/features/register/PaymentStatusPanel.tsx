import { Button } from '@/components/ui/Button'
import { StatusBadge } from '@/components/StatusBadge'
import type { PaymentResponse } from '@/api/types'

export function PaymentStatusPanel({
  payment,
  onRetry,
  polling,
}: {
  payment: PaymentResponse | null | undefined
  onRetry?: () => void
  polling?: boolean
}) {
  if (!payment) {
    return <p className="text-sm text-ink-muted">No payment record yet.</p>
  }

  return (
    <div className="rounded-lg border border-border p-4 space-y-2">
      <div className="flex items-center justify-between">
        <span className="font-medium">Payment</span>
        <StatusBadge status={payment.status} />
      </div>
      {polling ? <p className="text-xs text-ink-muted">Checking status…</p> : null}
      {payment.status === 'FAILED' && onRetry ? (
        <Button type="button" onClick={onRetry}>Try payment again</Button>
      ) : null}
    </div>
  )
}
