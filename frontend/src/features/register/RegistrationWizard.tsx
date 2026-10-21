import { getEvent } from '@/api/events'
import { createOrder, getPaymentByRegistration } from '@/api/payments'
import { registerForEvent } from '@/api/registrations'
import { createTeam, getTeamMembers, listMyTeams } from '@/api/teams'
import type { EventRegistrationResponse, EventType } from '@/api/types'
import { Button } from '@/components/ui/Button'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { PaymentStatusPanel } from '@/features/register/PaymentStatusPanel'
import { ProfileGate } from '@/features/register/ProfileGate'
import { useAuth } from '@/hooks/useAuth'
import { useRazorpayCheckout } from '@/hooks/useRazorpayCheckout'
import { addEntry, getEntryForEvent, updateEntryStatus } from '@/lib/entries-store'
import { doneStepIndex, paymentStepIndex } from '@/lib/registration-steps'
import { toIsoStringOrEpoch } from '@/lib/iso-datetime'
import {
  isProfileComplete,
  isTicketConfirmed,
  ticketStatusMessage,
  getTicketBlockers,
} from '@/lib/ticket-eligibility'
import { ApiError } from '@/api/client'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useMemo, useRef, useState } from 'react'
import { useToast } from '@/components/ui/Toast'
import { Link, useNavigate, useParams } from 'react-router-dom'

const resumeKey = (userId: number, eventId: number) => `ems.register.${userId}.${eventId}`

type ResumeState = {
  step: number
  registration?: EventRegistrationResponse | null
  teamId?: number
}

export function RegistrationWizard() {
  const { eventId: rawId } = useParams()
  const eventId = Number(rawId)
  const { user } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { openCheckout, busy } = useRazorpayCheckout()
  const { toast } = useToast()

  const [step, setStep] = useState(0)
  const [registration, setRegistration] = useState<EventRegistrationResponse | null>(null)
  const [teamId, setTeamId] = useState<number | undefined>()
  const [newTeamName, setNewTeamName] = useState('')
  const [pollPayment, setPollPayment] = useState(false)
  const [resumed, setResumed] = useState(false)
  const resumeLoadedKey = useRef<string | null>(null)

  const {
    data: event,
    isLoading: eventLoading,
    isError: eventError,
  } = useQuery({
    queryKey: ['events', eventId],
    queryFn: () => getEvent(eventId),
    enabled: Number.isFinite(eventId),
  })

  const { data: myTeams = [] } = useQuery({
    queryKey: ['teams', 'me'],
    queryFn: listMyTeams,
  })

  const teamsForEvent = useMemo(
    () => myTeams.filter((t) => t.eventId === eventId),
    [myTeams, eventId],
  )

  function registrationFromStored(entry: {
    registrationId: number
    eventId: number
    registeredAt: string
    registrationStatus?: EventRegistrationResponse['status']
  }): EventRegistrationResponse {
    return {
      id: entry.registrationId,
      eventId: entry.eventId,
      userId: user?.id ?? 0,
      teamId: null,
      status: entry.registrationStatus ?? 'PENDING',
      registeredAt: entry.registeredAt,
    }
  }

  function goToResumeStep(reg: EventRegistrationResponse, eventType: EventType, fee: number) {
    if (fee > 0 && reg.status === 'PENDING') {
      setStep(paymentStepIndex(eventType))
    } else if (reg.status === 'REGISTERED') {
      setStep(doneStepIndex(eventType))
    }
  }

  useEffect(() => {
    if (!user?.id || !Number.isFinite(eventId)) return
    const loadKey = `${user.id}:${eventId}`
    if (resumeLoadedKey.current === loadKey) return
    resumeLoadedKey.current = loadKey
    setStep(0)
    setRegistration(null)
    setResumed(false)

    let reg: EventRegistrationResponse | null = null
    let savedStep: number | undefined
    let savedTeamId: number | undefined
    let fromEntry = false

    const raw = sessionStorage.getItem(resumeKey(user.id, eventId))
    if (raw) {
      try {
        const parsed = JSON.parse(raw) as ResumeState
        savedStep = parsed.step
        if (parsed.registration?.userId === user.id) reg = parsed.registration
        savedTeamId = parsed.teamId
      } catch {
        /* ignore */
      }
    }

    const stored = getEntryForEvent(user.id, eventId)
    if (stored) {
      reg = registrationFromStored(stored)
      fromEntry = true
    }

    if (reg) {
      setRegistration(reg)
      setResumed(fromEntry || Boolean(savedStep && savedStep > 0))
      if (savedTeamId) setTeamId(savedTeamId)
      // Step is corrected once event + payment load (see effects below).
      if (savedStep !== undefined && savedStep > 0) {
        setStep(savedStep)
      }
    }
  }, [eventId, user?.id])

  const paymentQuery = useQuery({
    queryKey: ['payments', registration?.id],
    queryFn: () => getPaymentByRegistration(registration!.id),
    enabled: Boolean(registration?.id),
    refetchInterval: pollPayment ? 3000 : false,
  })

  useEffect(() => {
    if (!registration?.id) return
    const payment = paymentQuery.data
    if (
      user?.id &&
      payment?.status === 'COMPLETED' &&
      registration.status === 'PENDING'
    ) {
      setRegistration({ ...registration, status: 'REGISTERED' })
      updateEntryStatus(user.id, registration.id, 'REGISTERED')
    }
  }, [paymentQuery.data, registration, user?.id])

  useEffect(() => {
    if (!event || !registration) return
    const fee = Number(event.registrationFee)
    const payStep = paymentStepIndex(event.type)
    const paymentDone =
      paymentQuery.data?.status === 'COMPLETED' || registration.status === 'REGISTERED'
    if (fee > 0 && registration.status === 'REGISTERED' && step !== doneStepIndex(event.type)) {
      setStep(doneStepIndex(event.type))
    }
    if (fee > 0 && registration.status === 'PENDING' && !paymentDone && step !== payStep) {
      setStep(payStep)
    }
  }, [event, registration, paymentQuery.data?.status, paymentQuery.isFetched, step])

  useEffect(() => {
    const state: ResumeState = { step, registration, teamId }
    if (!user?.id) return
    sessionStorage.setItem(resumeKey(user.id, eventId), JSON.stringify(state))
  }, [step, registration, teamId, eventId, user?.id])

  useEffect(() => {
    if (!registration || paymentQuery.data?.status !== 'COMPLETED') return
    if (registration.status === 'PENDING') {
      const next = { ...registration, status: 'REGISTERED' as const }
      setRegistration(next)
      if (user?.id) updateEntryStatus(user.id, registration.id, 'REGISTERED')
      if (event) setStep(doneStepIndex(event.type))
    }
  }, [paymentQuery.data?.status, registration, event, user?.id])

  const registerMutation = useMutation({
    mutationFn: async () => {
      if (!event) throw new Error('No event')
      if (!isProfileComplete(user)) {
        throw new Error('Verify email and phone on your profile before registering')
      }
      if (event.type === 'TEAM') {
        if (!teamId) throw new Error('Select a team')
        const members = await getTeamMembers(eventId, teamId)
        const min = event.minTeamSize ?? 1
        if (members.length < min) {
          throw new Error(`Team needs at least ${min} members (has ${members.length})`)
        }
      }
      return registerForEvent(eventId, { teamId: event.type === 'TEAM' ? teamId : null })
    },
    onSuccess: (reg) => {
      setRegistration(reg)
      if (user?.id) {
        addEntry(user.id, {
          registrationId: reg.id,
          eventId: reg.eventId,
          registeredAt: toIsoStringOrEpoch(reg.registeredAt),
          registrationStatus: reg.status,
        })
      }
      const fee = Number(event?.registrationFee ?? 0)
      if (event) {
        if (fee > 0 && reg.status === 'PENDING') {
          setStep(paymentStepIndex(event.type))
        } else {
          setStep(doneStepIndex(event.type))
        }
      }
      void queryClient.invalidateQueries({ queryKey: ['registrations', 'mine'] })
    },
    onError: (e: unknown) => {
      const alreadyRegistered =
        e instanceof ApiError &&
        (e.status === 409 || e.message.toLowerCase().includes('already registered'))
      if (alreadyRegistered && event) {
        const stored = user?.id ? getEntryForEvent(user.id, eventId) : undefined
        if (stored) {
          const reg = registrationFromStored(stored)
          setRegistration(reg)
          setResumed(true)
          goToResumeStep(reg, event.type, Number(event.registrationFee))
          toast('Continuing your existing registration')
          return
        }
      }
      toast(e instanceof Error ? e.message : 'Registration failed', 'error')
    },
  })

  const createTeamMutation = useMutation({
    mutationFn: () => {
      if (teamsForEvent.length > 0) {
        throw new Error(
          'You are already on a team for this event. Select it above or leave it before creating another.',
        )
      }
      return createTeam(eventId, { name: newTeamName })
    },
    onSuccess: (team) => {
      setTeamId(team.id)
      void queryClient.invalidateQueries({ queryKey: ['teams'] })
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const payMutation = useMutation({
    mutationFn: async () => {
      if (!registration) throw new Error('No registration')
      if (!isProfileComplete(user)) {
        throw new Error('Verify email and phone on your profile before paying')
      }
      const order = await createOrder({ registrationId: registration.id })
      await openCheckout(order, {
        name: user?.name,
        email: user?.email,
        onSuccess: () => {
          setPollPayment(false)
          setRegistration((r) => (r ? { ...r, status: 'REGISTERED' } : r))
          if (user?.id) {
            updateEntryStatus(user.id, registration.id, 'REGISTERED')
            addEntry(user.id, {
              registrationId: registration.id,
              eventId: registration.eventId,
              registeredAt: toIsoStringOrEpoch(registration.registeredAt),
              registrationStatus: 'REGISTERED',
            })
          }
          void queryClient.invalidateQueries({ queryKey: ['payments', registration.id] })
          void queryClient.invalidateQueries({ queryKey: ['registrations', 'mine'] })
          setStep(doneStepIndex(event!.type))
        },
        onFailure: () => {
          setPollPayment(true)
          void queryClient.invalidateQueries({ queryKey: ['payments', registration.id] })
        },
      })
    },
  })

  if (!Number.isFinite(eventId)) {
    return <p className="text-signal">Invalid event.</p>
  }
  if (eventLoading) {
    return <p className="text-ink-muted">Loading event…</p>
  }
  if (eventError || !event) {
    return <p className="text-signal">Could not load this event.</p>
  }

  const steps =
    event.type === 'TEAM'
      ? ['Mode', 'Team', 'Review', 'Payment', 'Done']
      : ['Mode', 'Review', 'Payment', 'Done']

  const fee = Number(event.registrationFee)
  const payStep = paymentStepIndex(event.type)
  const paymentComplete =
    paymentQuery.data?.status === 'COMPLETED' || registration?.status === 'REGISTERED'
  const needsPayment =
    Boolean(registration) &&
    fee > 0 &&
    registration!.status === 'PENDING' &&
    !paymentComplete
  const profileComplete = isProfileComplete(user)
  const ticketConfirmed =
    registration &&
    isTicketConfirmed(
      fee,
      registration.status,
      paymentQuery.data?.status,
      profileComplete,
    )
  const ticketNote =
    registration
      ? ticketStatusMessage(
          getTicketBlockers(fee, registration.status, paymentQuery.data?.status, profileComplete),
        )
      : null

  return (
    <div className="mx-auto max-w-xl space-y-6">
      <h1 className="text-3xl font-bold">Register — {event.name}</h1>
      <ProfileGate user={user} />
      {resumed && registration && fee > 0 && registration.status === 'PENDING' ? (
        <p className="rounded-lg border border-stamp/30 bg-surface px-4 py-3 text-sm">
          You have an unfinished registration for this event. Continue with payment below — your
          progress is saved on this device.
        </p>
      ) : null}
      <ol className="flex gap-2 text-xs uppercase tracking-wide text-ink-muted">
        {steps.map((s, i) => (
          <li key={s} className={i === step ? 'text-stamp font-semibold' : ''}>{s}</li>
        ))}
      </ol>

      {step === 0 && (
        <div className="space-y-4">
          <p>{event.type === 'TEAM' ? 'This is a team event. You will pick or create a team next.' : 'Solo registration.'}</p>
          <Button
            type="button"
            disabled={!profileComplete}
            onClick={() => setStep(1)}
          >
            Continue
          </Button>
        </div>
      )}

      {event.type === 'TEAM' && step === 1 && (
        <div className="space-y-4">
          <Field label="Your teams for this event">
            <div className="space-y-2">
              {teamsForEvent.map((t) => (
                <button
                  key={t.id}
                  type="button"
                  className={`w-full rounded border px-3 py-2 text-left text-sm ${teamId === t.id ? 'border-stamp' : 'border-border'}`}
                  onClick={() => setTeamId(t.id)}
                >
                  {t.name}
                </button>
              ))}
              {!teamsForEvent.length ? (
                <p className="text-sm text-ink-muted">No teams yet — create one below.</p>
              ) : null}
            </div>
          </Field>
          {teamsForEvent.length === 0 ? (
            <>
              <Field label="New team name">
                <Input value={newTeamName} onChange={(e) => setNewTeamName(e.target.value)} />
              </Field>
              <Button
                type="button"
                variant="secondary"
                disabled={!newTeamName || createTeamMutation.isPending}
                onClick={() => createTeamMutation.mutate()}
              >
                Create team
              </Button>
            </>
          ) : (
            <p className="text-sm text-ink-muted">
              You are already on a team for this event. Select it above to continue registration.
            </p>
          )}
          <Button type="button" disabled={!teamId} onClick={() => setStep(2)}>Continue</Button>
        </div>
      )}

      {((event.type === 'SOLO' && step === 1) || (event.type === 'TEAM' && step === 2)) && (
        <div className="space-y-4">
          <p className="text-sm text-ink-muted">Confirm registration for {event.name}.</p>
          {event.type === 'TEAM' ? <p className="text-sm">Team ID: {teamId}</p> : null}
          <Button
            type="button"
            onClick={() => registerMutation.mutate()}
            disabled={registerMutation.isPending || !profileComplete}
          >
            Submit registration
          </Button>
          {registerMutation.error ? (
            <p className="text-signal text-sm">{(registerMutation.error as Error).message}</p>
          ) : null}
        </div>
      )}

      {needsPayment && step === payStep ? (
          <div className="space-y-4">
            <p className="text-sm text-ink-muted tabular">
              Amount due: ₹{fee.toLocaleString('en-IN')}
            </p>
            <PaymentStatusPanel
              payment={paymentQuery.data}
              polling={pollPayment}
              onRetry={() => payMutation.mutate()}
            />
            <Button
              type="button"
              disabled={busy || payMutation.isPending || !profileComplete}
              onClick={() => payMutation.mutate()}
            >
              Pay with Razorpay
            </Button>
            {payMutation.error ? (
              <p className="text-signal text-sm">{(payMutation.error as Error).message}</p>
            ) : null}
          </div>
        ) : null}

      {needsPayment && step !== payStep ? (
        <div className="space-y-2">
          <p className="text-sm text-stamp">Payment is required to complete registration.</p>
          <Button type="button" onClick={() => setStep(payStep)}>
            Go to payment
          </Button>
        </div>
      ) : null}

      {step === steps.length - 1 && registration ? (
        <div className="space-y-4 motion-safe:animate-[fadeIn_0.4s_ease]">
          {ticketConfirmed ? (
            <p className="text-mint font-medium">Your ticket is confirmed.</p>
          ) : (
            <>
              <p className="font-medium">Registration saved</p>
              {ticketNote ? <p className="text-sm text-stamp">{ticketNote}</p> : null}
            </>
          )}
          <Link to={`/entries/${registration.id}`}>
            <Button>View pass</Button>
          </Link>
          <Button variant="ghost" type="button" onClick={() => navigate('/entries')}>
            All entries
          </Button>
        </div>
      ) : null}

      {registration && Number(event.registrationFee) <= 0 && registration.status === 'REGISTERED' && step < steps.length - 1 ? (
        <Button type="button" onClick={() => setStep(steps.length - 1)}>Finish</Button>
      ) : null}
    </div>
  )
}
