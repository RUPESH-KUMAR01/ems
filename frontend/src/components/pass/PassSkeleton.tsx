import { Skeleton } from '@/components/ui/Skeleton'

export function PassSkeleton() {
  return (
    <div className="space-y-3 rounded-lg border border-border bg-surface p-5">
      <Skeleton className="h-6 w-2/3" />
      <Skeleton className="h-4 w-1/2" />
      <Skeleton className="h-12 w-full" />
    </div>
  )
}
