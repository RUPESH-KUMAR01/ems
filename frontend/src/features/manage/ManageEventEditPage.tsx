import {
  cancelEvent,
  completeEvent,
  createEvent,
  deleteEvent,
  getManagedEvent,
  publishEvent,
  updateEvent,
} from '@/api/events'
import { Button } from '@/components/ui/Button'
import { ConfirmDialog } from '@/components/ui/Dialog'
import { EventForm, onEventSaved, type EventFormValues } from '@/features/manage/EventForm'
import {
  toCreateEventPayload,
  toUpdateEventPayload,
  validateEventForm,
  type EventFormFieldErrors,
} from '@/features/manage/event-form-schema'
import { useToast } from '@/components/ui/Toast'
import { StatusBadge } from '@/components/StatusBadge'
import { formatApiError } from '@/lib/format-api-error'
import {
  allowedLifecycleActions,
  lifecycleSuccessMessage,
  type LifecycleAction,
} from '@/lib/event-lifecycle'
import type { EventResponse } from '@/api/types'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'

function syncEventCaches(queryClient: ReturnType<typeof useQueryClient>, ev: EventResponse) {
  queryClient.setQueryData(['events', ev.id, 'manage'], ev)
  queryClient.setQueryData(['events', ev.id], ev)
  void queryClient.invalidateQueries({ queryKey: ['events', 'me'] })
  void queryClient.invalidateQueries({ queryKey: ['events', 'visible'] })
}

function runClientValidation(
  values: EventFormValues,
  setFieldErrors: (e: EventFormFieldErrors) => void,
  toast: (msg: string, variant?: 'error') => void,
): boolean {
  const result = validateEventForm(values)
  if (result.ok) {
    setFieldErrors({})
    return true
  }
  setFieldErrors(result.fieldErrors)
  toast(result.summary, 'error')
  return false
}

export function ManageEventNewPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { toast } = useToast()
  const [values, setValues] = useState<EventFormValues | null>(null)
  const [fieldErrors, setFieldErrors] = useState<EventFormFieldErrors>({})
  const create = useMutation({
    mutationFn: (payload: EventFormValues) => createEvent(toCreateEventPayload(payload)),
    onSuccess: (ev) => {
      onEventSaved('new', ev)
      setFieldErrors({})
      syncEventCaches(queryClient, ev)
      toast('Event created')
      navigate(`/manage/events/${ev.id}/edit`)
    },
    onError: (e: unknown) => toast(formatApiError(e), 'error'),
  })

  const handleSaveDraft = () => {
    if (!values) return
    if (!runClientValidation(values, setFieldErrors, toast)) return
    create.mutate(values)
  }

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">New event</h1>
      <EventForm eventId="new" errors={fieldErrors} onChange={setValues} />
      <Button type="button" disabled={!values || create.isPending} onClick={handleSaveDraft}>
        Save draft
      </Button>
    </div>
  )
}

export function ManageEventEditPage() {
  const { eventId: raw } = useParams()
  const eventId = Number(raw)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { toast } = useToast()
  const [values, setValues] = useState<EventFormValues | null>(null)
  const [fieldErrors, setFieldErrors] = useState<EventFormFieldErrors>({})
  const [confirmDelete, setConfirmDelete] = useState(false)

  const event = useQuery({
    queryKey: ['events', eventId, 'manage'],
    queryFn: () => getManagedEvent(eventId),
    enabled: Number.isFinite(eventId),
  })

  const save = useMutation({
    mutationFn: (payload: EventFormValues) => updateEvent(eventId, toUpdateEventPayload(payload)),
    onSuccess: (ev) => {
      onEventSaved(eventId, ev)
      setFieldErrors({})
      syncEventCaches(queryClient, ev)
      toast('Saved')
    },
    onError: (e: unknown) => toast(formatApiError(e), 'error'),
  })

  const lifecycle = useMutation({
    mutationFn: (action: LifecycleAction) => {
      if (action === 'publish') return publishEvent(eventId)
      if (action === 'cancel') return cancelEvent(eventId)
      return completeEvent(eventId)
    },
    onSuccess: (ev, action) => {
      syncEventCaches(queryClient, ev)
      toast(lifecycleSuccessMessage(action))
    },
    onError: (e: unknown) => toast(formatApiError(e), 'error'),
  })

  const remove = useMutation({
    mutationFn: () => deleteEvent(eventId),
    onSuccess: () => {
      toast('Deleted')
      navigate('/manage/events')
    },
    onError: (e: unknown) => toast(formatApiError(e), 'error'),
  })

  if (!Number.isFinite(eventId)) {
    return (
      <div className="space-y-2">
        <p className="text-signal">Invalid event ID.</p>
        <Link to="/manage/events" className="text-sm underline text-ink-muted">Back to manage events</Link>
      </div>
    )
  }
  if (event.isLoading) return <p className="text-ink-muted">Loading…</p>
  if (event.isError || !event.data) {
    return (
      <div className="space-y-2">
        <p className="text-signal">
          {event.error ? formatApiError(event.error) : 'Event not found.'}
        </p>
        <Link to="/manage/events" className="text-sm underline text-ink-muted">Back to manage events</Link>
      </div>
    )
  }

  const handleSave = () => {
    if (!values) return
    if (!runClientValidation(values, setFieldErrors, toast)) return
    save.mutate(values)
  }

  const status = event.data.status
  const actions = allowedLifecycleActions(status)

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center gap-3">
        <h1 className="text-3xl font-bold">Edit — {event.data.name}</h1>
        <StatusBadge status={status} />
      </div>
      <EventForm
        eventId={eventId}
        errors={fieldErrors}
        initial={{
          ...event.data,
          minTeamSize: event.data.minTeamSize ?? undefined,
          maxTeamSize: event.data.maxTeamSize ?? undefined,
        }}
        onChange={setValues}
      />
      <div className="flex flex-wrap gap-2">
        <Button type="button" disabled={!values || save.isPending} onClick={handleSave}>
          Save
        </Button>
        {actions.includes('publish') ? (
          <Button
            type="button"
            variant="secondary"
            disabled={lifecycle.isPending}
            onClick={() => lifecycle.mutate('publish')}
          >
            Publish
          </Button>
        ) : null}
        {actions.includes('cancel') ? (
          <Button
            type="button"
            variant="ghost"
            disabled={lifecycle.isPending}
            onClick={() => lifecycle.mutate('cancel')}
          >
            Cancel event
          </Button>
        ) : null}
        {actions.includes('complete') ? (
          <Button
            type="button"
            variant="ghost"
            disabled={lifecycle.isPending}
            onClick={() => lifecycle.mutate('complete')}
          >
            Complete
          </Button>
        ) : null}
        <Button type="button" variant="danger" onClick={() => setConfirmDelete(true)}>
          Delete
        </Button>
      </div>
      <p className="text-sm text-ink-muted">
        <Link to={`/events/${eventId}/teams`} className="underline">
          View teams for this event
        </Link>{' '}
        — registration lists require admin API.
      </p>
      <ConfirmDialog
        open={confirmDelete}
        onOpenChange={setConfirmDelete}
        title="Delete event?"
        description="This cannot be undone."
        destructive
        confirmLabel="Delete"
        onConfirm={() => remove.mutate()}
      />
    </div>
  )
}
