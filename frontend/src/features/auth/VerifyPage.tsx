import { generateEmailOtp, generatePhoneOtp, verifyEmail, verifyPhone } from '@/api/auth'
import { Button } from '@/components/ui/Button'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { useToast } from '@/components/ui/Toast'
import { useAuth } from '@/hooks/useAuth'
import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'

export function VerifyPage() {
  const { user, refreshUser } = useAuth()
  const { toast } = useToast()
  const [emailOtp, setEmailOtp] = useState('')
  const [phoneOtp, setPhoneOtp] = useState('')

  const emailVerify = useMutation({
    mutationFn: () => verifyEmail({ otp: emailOtp }),
    onSuccess: async () => {
      toast('Email verified')
      await refreshUser()
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const phoneVerify = useMutation({
    mutationFn: () => verifyPhone({ otp: phoneOtp }),
    onSuccess: async () => {
      toast('Phone verified')
      await refreshUser()
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  const genEmail = useMutation({ mutationFn: generateEmailOtp, onSuccess: () => toast('Email OTP sent') })
  const genPhone = useMutation({ mutationFn: generatePhoneOtp, onSuccess: () => toast('Phone OTP sent') })

  return (
    <div className="mx-auto max-w-lg space-y-8">
      <h1 className="text-3xl font-bold">Verification</h1>
      <p className="text-ink-muted text-sm">
        Signed in as {user?.email}. Request OTP from the backend, then enter it below.
      </p>

      <section className="space-y-3 rounded-lg border border-border p-4">
        <h2 className="font-medium">Email {user?.emailVerified ? '(verified)' : ''}</h2>
        <Button type="button" variant="ghost" onClick={() => genEmail.mutate()} disabled={genEmail.isPending}>
          Send email OTP
        </Button>
        <Field label="Email OTP">
          <Input value={emailOtp} onChange={(e) => setEmailOtp(e.target.value)} />
        </Field>
        <Button type="button" onClick={() => emailVerify.mutate()} disabled={emailVerify.isPending}>
          Verify email
        </Button>
      </section>

      <section className="space-y-3 rounded-lg border border-border p-4">
        <h2 className="font-medium">Phone {user?.phoneVerified ? '(verified)' : ''}</h2>
        <Button type="button" variant="ghost" onClick={() => genPhone.mutate()} disabled={genPhone.isPending}>
          Send phone OTP
        </Button>
        <Field label="Phone OTP">
          <Input value={phoneOtp} onChange={(e) => setPhoneOtp(e.target.value)} />
        </Field>
        <Button type="button" onClick={() => phoneVerify.mutate()} disabled={phoneVerify.isPending}>
          Verify phone
        </Button>
      </section>
    </div>
  )
}
