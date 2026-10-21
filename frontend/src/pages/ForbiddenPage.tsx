import { Link } from 'react-router-dom'

export function ForbiddenPage() {
  return (
    <div className="space-y-4 text-center py-16">
      <h1 className="text-3xl font-bold">403</h1>
      <p className="text-ink-muted">You don&apos;t have permission to view this page.</p>
      <Link to="/" className="text-stamp underline">Home</Link>
    </div>
  )
}
