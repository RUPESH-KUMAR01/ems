import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <div className="space-y-4 text-center py-16">
      <h1 className="text-3xl font-bold">404</h1>
      <p className="text-ink-muted">Page not found.</p>
      <Link to="/" className="text-stamp underline">Home</Link>
    </div>
  )
}
