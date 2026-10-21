import type { CreateEventRequest, UpdateEventRequest } from '@/api/types'
import type { EventFormValues } from '@/features/manage/event-form-types'
import { z } from 'zod'

const eventFormSchema = z
  .object({
    name: z.string().trim().min(1, 'Event name is required'),
    description: z.string().trim().min(1, 'Description is required'),
    type: z.enum(['SOLO', 'TEAM']),
    visibility: z.enum(['PRIVATE', 'PUBLIC']).optional(),
    maxParticipants: z.number().int().min(1, 'Need at least one participant'),
    minTeamSize: z.number().int().min(1).optional(),
    maxTeamSize: z.number().int().min(1).optional(),
    registrationFee: z.number().min(0, 'Registration fee cannot be negative').optional(),
    registrationDeadline: z.string().min(1, 'Registration deadline is required'),
    startTime: z.string().min(1, 'Event start time is required'),
    endTime: z.string().min(1, 'Event end time is required'),
  })
  .superRefine((data, ctx) => {
    const deadline = Date.parse(data.registrationDeadline)
    const start = Date.parse(data.startTime)
    const end = Date.parse(data.endTime)
    const now = Date.now()

    if (!Number.isFinite(deadline)) {
      ctx.addIssue({
        code: 'custom',
        message: 'Enter a valid registration deadline',
        path: ['registrationDeadline'],
      })
    }
    if (!Number.isFinite(start)) {
      ctx.addIssue({ code: 'custom', message: 'Enter a valid start time', path: ['startTime'] })
    }
    if (!Number.isFinite(end)) {
      ctx.addIssue({ code: 'custom', message: 'Enter a valid end time', path: ['endTime'] })
    }
    if (Number.isFinite(deadline) && deadline <= now) {
      ctx.addIssue({
        code: 'custom',
        message: 'Registration deadline must be in the future',
        path: ['registrationDeadline'],
      })
    }
    if (Number.isFinite(deadline) && Number.isFinite(start) && deadline >= start) {
      ctx.addIssue({
        code: 'custom',
        message: 'Registration must close before the event starts (deadline before start time)',
        path: ['registrationDeadline'],
      })
    }
    if (Number.isFinite(start) && Number.isFinite(end) && start >= end) {
      ctx.addIssue({
        code: 'custom',
        message: 'End time must be after start time',
        path: ['endTime'],
      })
    }
    if (data.type === 'TEAM') {
      if (data.minTeamSize == null) {
        ctx.addIssue({
          code: 'custom',
          message: 'Min team size is required for team events',
          path: ['minTeamSize'],
        })
      }
      if (data.maxTeamSize == null) {
        ctx.addIssue({
          code: 'custom',
          message: 'Max team size is required for team events',
          path: ['maxTeamSize'],
        })
      }
      if (
        data.minTeamSize != null &&
        data.maxTeamSize != null &&
        data.minTeamSize > data.maxTeamSize
      ) {
        ctx.addIssue({
          code: 'custom',
          message: 'Minimum team size cannot exceed maximum',
          path: ['maxTeamSize'],
        })
      }
    }
  })

export type EventFormFieldErrors = Partial<Record<keyof EventFormValues, string>>

export function validateEventForm(
  values: EventFormValues,
): { ok: true } | { ok: false; fieldErrors: EventFormFieldErrors; summary: string } {
  const result = eventFormSchema.safeParse(values)
  if (result.success) return { ok: true }

  const fieldErrors: EventFormFieldErrors = {}
  for (const issue of result.error.issues) {
    const key = issue.path[0]
    if (typeof key === 'string' && !fieldErrors[key as keyof EventFormValues]) {
      fieldErrors[key as keyof EventFormValues] = issue.message
    }
  }
  const summary = result.error.issues.map((i) => i.message).join(' · ')
  return { ok: false, fieldErrors, summary }
}

export function toCreateEventPayload(values: EventFormValues): CreateEventRequest {
  const payload: CreateEventRequest = {
    name: values.name.trim(),
    description: values.description.trim(),
    type: values.type,
    visibility: values.visibility,
    maxParticipants: values.maxParticipants,
    registrationFee: values.registrationFee ?? 0,
    registrationDeadline: values.registrationDeadline,
    startTime: values.startTime,
    endTime: values.endTime,
  }
  if (values.type === 'TEAM') {
    payload.minTeamSize = values.minTeamSize
    payload.maxTeamSize = values.maxTeamSize
  }
  return payload
}

export function toUpdateEventPayload(values: EventFormValues): UpdateEventRequest {
  const payload = toCreateEventPayload(values) as UpdateEventRequest
  if (values.type === 'SOLO') {
    delete payload.minTeamSize
    delete payload.maxTeamSize
  }
  return payload
}
