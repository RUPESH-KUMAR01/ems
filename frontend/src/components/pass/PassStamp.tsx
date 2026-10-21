import { cn } from '@/lib/cn'

export type StampVariant =
  | 'paid'
  | 'pending'
  | 'failed'
  | 'cancelled'
  | 'draft'
  | 'registered'
  | 'free'

const labels: Record<StampVariant, string> = {
  paid: 'Paid',
  pending: 'Pending',
  failed: 'Failed',
  cancelled: 'Cancelled',
  draft: 'Draft',
  registered: 'Registered',
  free: 'Free entry',
}

const styles: Record<StampVariant, string> = {
  paid: 'border-mint text-mint rotate-[-8deg]',
  registered: 'border-mint text-mint rotate-[-6deg]',
  free: 'border-stamp text-stamp rotate-[-4deg]',
  pending: 'border-stamp text-stamp rotate-[6deg]',
  failed: 'border-signal text-signal rotate-[8deg]',
  cancelled: 'border-ink-muted text-ink-muted rotate-[4deg]',
  draft: 'border-ink-muted text-ink-muted rotate-[-10deg]',
}

export function PassStamp({ variant }: { variant: StampVariant }) {
  return (
    <div
      className={cn(
        'absolute right-4 top-4 rounded border-2 border-dashed px-3 py-1 font-display text-xs font-bold uppercase tracking-widest',
        styles[variant],
      )}
      aria-label={labels[variant]}
    >
      {labels[variant]}
    </div>
  )
}
