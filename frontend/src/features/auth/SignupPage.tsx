import { register } from '@/api/auth'
import { Button } from '@/components/ui/Button'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { useToast } from '@/components/ui/Toast'
import { useAuth } from '@/hooks/useAuth'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { z } from 'zod'

const schema = z.object({
  name: z.string().min(1),
  email: z.string().email(),
  password: z.string().min(6),
  phone: z.string().min(1),
})

type Form = z.infer<typeof schema>

export function SignupPage() {
  const navigate = useNavigate()
  const { refreshUser } = useAuth()
  const { toast } = useToast()
  const form = useForm<Form>({ resolver: zodResolver(schema) })

  const mutation = useMutation({
    mutationFn: register,
    onSuccess: async () => {
      await refreshUser()
      navigate('/verify')
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  return (
    <div className="mx-auto max-w-md space-y-6">
      <h1 className="text-3xl font-bold">Sign up</h1>
      <form className="space-y-4" onSubmit={form.handleSubmit((v) => mutation.mutate(v))}>
        <Field label="Name" error={form.formState.errors.name?.message}>
          <Input {...form.register('name')} />
        </Field>
        <Field label="Email" error={form.formState.errors.email?.message}>
          <Input type="email" {...form.register('email')} />
        </Field>
        <Field label="Phone" error={form.formState.errors.phone?.message}>
          <Input {...form.register('phone')} />
        </Field>
        <Field label="Password" error={form.formState.errors.password?.message}>
          <Input type="password" {...form.register('password')} />
        </Field>
        <Button type="submit" disabled={mutation.isPending} className="w-full">
          {mutation.isPending ? 'Creating…' : 'Create account'}
        </Button>
      </form>
      <p className="text-sm text-ink-muted">
        Already registered? <Link to="/login" className="text-stamp underline">Log in</Link>
      </p>
    </div>
  )
}
