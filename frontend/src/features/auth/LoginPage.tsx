import { login } from '@/api/auth'
import { Button } from '@/components/ui/Button'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { useToast } from '@/components/ui/Toast'
import { useAuth } from '@/hooks/useAuth'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { z } from 'zod'

const schema = z.object({
  email: z.string().email(),
  password: z.string().min(6),
})

type Form = z.infer<typeof schema>

export function LoginPage() {
  const [params] = useSearchParams()
  const redirect = params.get('redirect') || '/events'
  const navigate = useNavigate()
  const { refreshUser } = useAuth()
  const { toast } = useToast()

  const form = useForm<Form>({ resolver: zodResolver(schema) })

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: async () => {
      await refreshUser()
      navigate(redirect)
    },
    onError: (e: Error) => toast(e.message, 'error'),
  })

  return (
    <div className="mx-auto max-w-md space-y-6">
      <h1 className="text-3xl font-bold">Log in</h1>
      <form
        className="space-y-4"
        onSubmit={form.handleSubmit((v) => mutation.mutate(v))}
      >
        <Field label="Email" error={form.formState.errors.email?.message}>
          <Input type="email" {...form.register('email')} />
        </Field>
        <Field label="Password" error={form.formState.errors.password?.message}>
          <Input type="password" {...form.register('password')} />
        </Field>
        <Button type="submit" disabled={mutation.isPending} className="w-full">
          {mutation.isPending ? 'Signing in…' : 'Sign in'}
        </Button>
      </form>
      <p className="text-sm text-ink-muted">
        No account? <Link to="/signup" className="text-stamp underline">Sign up</Link>
      </p>
    </div>
  )
}
