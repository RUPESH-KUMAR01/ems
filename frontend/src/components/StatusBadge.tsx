import { Badge } from '@/components/ui/Badge'
import { cn } from '@/lib/cn'

const tone: Record<string, string> = {
  REGISTERED: 'border-mint text-mint',
  COMPLETED: 'border-mint text-mint',
  APPROVED: 'border-mint text-mint',
  PENDING: 'border-stamp text-stamp',
  FAILED: 'border-signal text-signal',
  CANCELLED: 'border-ink-muted text-ink-muted',
  REJECTED: 'border-signal text-signal',
  DRAFT: 'border-ink-muted text-ink-muted',
  PUBLISHED: 'border-stamp text-stamp',
}

export function StatusBadge({ status }: { status: string }) {
  return <Badge className={cn(tone[status] ?? '')}>{status}</Badge>
}
