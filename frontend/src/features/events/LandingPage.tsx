import { listVisibleEvents } from '@/api/events'
import { PassGrid } from '@/components/pass/PassGrid'
import { PassSkeleton } from '@/components/pass/PassSkeleton'
import { Button } from '@/components/ui/Button'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'

export function LandingPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['events', 'visible'],
    queryFn: listVisibleEvents,
  })

  const preview = data?.slice(0, 3) ?? []

  return (
    <div className="space-y-10">
      <section className="grid gap-6 md:grid-cols-2 md:items-center">
        <div className="space-y-4">
          <h1 className="text-4xl font-bold leading-tight md:text-5xl">
            Your event pass, from browse to checkout.
          </h1>
          <p className="text-ink-muted max-w-prose">
            Discover public events, register solo or with a team, and pay securely with Razorpay.
          </p>
          <div className="flex gap-3">
            <Link to="/events"><Button>Browse events</Button></Link>
            <Link to="/signup"><Button variant="ghost">Create account</Button></Link>
          </div>
        </div>
        <div className="rounded-xl border border-dashed border-stamp/40 bg-surface p-6">
          <p className="font-display text-sm uppercase tracking-widest text-stamp">Live passes</p>
          {isLoading ? (
            <div className="mt-4 space-y-3">
              <PassSkeleton />
              <PassSkeleton />
            </div>
          ) : isError ? (
            <p className="mt-4 text-sm text-signal">Could not load events.</p>
          ) : preview.length ? (
            <div className="mt-4 space-y-3">
              {preview.map((e) => (
                <Link key={e.id} to={`/events/${e.id}`} className="block text-sm font-medium hover:underline">
                  {e.name}
                </Link>
              ))}
            </div>
          ) : (
            <p className="mt-4 text-sm text-ink-muted">No published events yet.</p>
          )}
        </div>
      </section>

      {data && data.length > 3 ? (
        <section>
          <h2 className="mb-4 text-2xl font-semibold">Featured passes</h2>
          <PassGrid events={preview} />
        </section>
      ) : null}
    </div>
  )
}
