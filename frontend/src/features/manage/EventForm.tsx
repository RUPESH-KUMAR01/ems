import type { EventResponse, EventType, EventVisibility } from '@/api/types'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Textarea } from '@/components/ui/Textarea'
import type { EventFormFieldErrors } from '@/features/manage/event-form-schema'
import type { EventFormValues } from '@/features/manage/event-form-types'
import { fromLocalDateTimeInput, toLocalDateTimeInput } from '@/lib/datetime-local'
import {
  clearEventSchedule,
  loadEventSchedule,
  saveEventSchedule,
} from '@/lib/event-schedule-store'
import { useEffect, useState } from 'react'

export type { EventFormValues } from '@/features/manage/event-form-types'

function defaultStartIso() {
  return new Date(Date.now() + 86400000 * 7).toISOString()
}

function defaultEndIso() {
  return new Date(Date.now() + 86400000 * 8).toISOString()
}

export function EventForm({
  eventId,
  initial,
  errors,
  onChange,
}: {
  eventId: number | 'new'
  initial?: Partial<EventFormValues>
  errors?: EventFormFieldErrors
  onChange: (values: EventFormValues) => void
}) {
  const schedule = loadEventSchedule(eventId)
  const [values, setValues] = useState<EventFormValues>({
    name: initial?.name ?? '',
    description: initial?.description ?? '',
    type: initial?.type ?? 'SOLO',
    visibility: initial?.visibility ?? 'PRIVATE',
    maxParticipants: initial?.maxParticipants ?? 100,
    minTeamSize: initial?.minTeamSize,
    maxTeamSize: initial?.maxTeamSize,
    registrationFee: initial?.registrationFee ?? 0,
    registrationDeadline:
      initial?.registrationDeadline ?? new Date(Date.now() + 86400000 * 30).toISOString(),
    startTime: schedule.startTime ?? initial?.startTime ?? defaultStartIso(),
    endTime: schedule.endTime ?? initial?.endTime ?? defaultEndIso(),
  })

  useEffect(() => {
    saveEventSchedule(eventId, { startTime: values.startTime, endTime: values.endTime })
  }, [eventId, values.startTime, values.endTime])

  const update = (patch: Partial<EventFormValues>) => {
    setValues((v) => {
      let next = { ...v, ...patch }
      if (patch.type === 'SOLO') {
        next = { ...next, minTeamSize: undefined, maxTeamSize: undefined }
      }
      onChange(next)
      return next
    })
  }

  useEffect(() => {
    onChange(values)
    // eslint-disable-next-line react-hooks/exhaustive-deps -- initial hydrate only
  }, [])

  return (
    <div className="grid gap-4 md:grid-cols-2">
      <Field label="Name" error={errors?.name}>
        <Input value={values.name} onChange={(e) => update({ name: e.target.value })} />
      </Field>
      <Field label="Visibility" error={errors?.visibility}>
        <Select
          value={values.visibility ?? 'PRIVATE'}
          onValueChange={(v) => update({ visibility: v as EventVisibility })}
          options={[
            { value: 'PRIVATE', label: 'Private' },
            { value: 'PUBLIC', label: 'Public' },
          ]}
        />
      </Field>
      <div className="md:col-span-2">
        <Field label="Description" error={errors?.description}>
          <Textarea value={values.description} onChange={(e) => update({ description: e.target.value })} />
        </Field>
      </div>
      <Field label="Type" error={errors?.type}>
        <Select
          value={values.type}
          onValueChange={(v) => update({ type: v as EventType })}
          options={[
            { value: 'SOLO', label: 'Solo' },
            { value: 'TEAM', label: 'Team' },
          ]}
        />
      </Field>
      <Field label="Max participants" error={errors?.maxParticipants}>
        <Input
          type="number"
          value={values.maxParticipants}
          onChange={(e) => update({ maxParticipants: Number(e.target.value) })}
        />
      </Field>
      {values.type === 'TEAM' ? (
        <>
          <Field label="Min team size" error={errors?.minTeamSize}>
            <Input
              type="number"
              value={values.minTeamSize ?? ''}
              onChange={(e) => update({ minTeamSize: Number(e.target.value) })}
            />
          </Field>
          <Field label="Max team size" error={errors?.maxTeamSize}>
            <Input
              type="number"
              value={values.maxTeamSize ?? ''}
              onChange={(e) => update({ maxTeamSize: Number(e.target.value) })}
            />
          </Field>
        </>
      ) : null}
      <Field label="Registration fee (INR)" error={errors?.registrationFee}>
        <Input
          type="number"
          value={values.registrationFee}
          onChange={(e) => update({ registrationFee: Number(e.target.value) })}
        />
      </Field>
      <Field label="Registration deadline" error={errors?.registrationDeadline}>
        <Input
          type="datetime-local"
          value={toLocalDateTimeInput(values.registrationDeadline)}
          onChange={(e) => {
            const iso = fromLocalDateTimeInput(e.target.value)
            if (iso) update({ registrationDeadline: iso })
          }}
        />
      </Field>
      <Field label="Start time" error={errors?.startTime}>
        <Input
          type="datetime-local"
          value={toLocalDateTimeInput(values.startTime)}
          onChange={(e) => {
            const iso = fromLocalDateTimeInput(e.target.value)
            if (iso) update({ startTime: iso })
          }}
        />
      </Field>
      <Field label="End time" error={errors?.endTime}>
        <Input
          type="datetime-local"
          value={toLocalDateTimeInput(values.endTime)}
          onChange={(e) => {
            const iso = fromLocalDateTimeInput(e.target.value)
            if (iso) update({ endTime: iso })
          }}
        />
      </Field>
      <p className="md:col-span-2 text-xs text-ink-muted">
        Start and end times are saved locally while you draft; they are sent to the server when you save.
      </p>
    </div>
  )
}

export function onEventSaved(eventId: number | 'new', saved: EventResponse) {
  const draft = loadEventSchedule(eventId)
  if (draft.startTime || draft.endTime) {
    saveEventSchedule(saved.id, draft)
    if (eventId !== saved.id) clearEventSchedule(eventId)
  }
}
