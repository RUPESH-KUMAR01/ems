import { Button } from '@/components/ui/Button'
import { isRouteErrorResponse, useRouteError } from 'react-router-dom'
import { Link } from 'react-router-dom'

export function RouteErrorPage() {
  const error = useRouteError()
  const message = isRouteErrorResponse(error)
    ? error.statusText || error.data?.toString()
    : error instanceof Error
      ? error.message
      : 'Something went wrong'

  return (
    <div className="mx-auto flex min-h-[50vh] max-w-md flex-col justify-center gap-4 px-4">
      <h1 className="font-display text-2xl font-semibold">This page hit an error</h1>
      <p className="text-sm text-ink-muted">{message}</p>
      <div className="flex gap-3">
        <Link to="/"><Button variant="ghost">Go home</Button></Link>
        <Button type="button" onClick={() => window.location.reload()}>Reload</Button>
      </div>
    </div>
  )
}
