import { Button } from '@/components/ui/Button'
import { isProfileComplete } from '@/lib/ticket-eligibility'
import type { UserResponse } from '@/api/types'
import { Link } from 'react-router-dom'

export function ProfileGate({ user }: { user: UserResponse | undefined }) {
  if (!user || isProfileComplete(user)) return null

  return (
    <div className="rounded-lg border border-stamp/40 bg-surface p-4 space-y-3">
      <p className="font-medium">Complete your profile before registering</p>
      <p className="text-sm text-ink-muted">
        Verify your email and phone. A ticket is only valid after your profile is complete and
        payment is successful (for paid events).
      </p>
      <ul className="text-sm text-ink-muted list-disc pl-5">
        <li>Email: {user.emailVerified ? 'Verified' : 'Not verified'}</li>
        <li>Phone: {user.phoneVerified ? 'Verified' : 'Not verified'}</li>
      </ul>
      <Link to="/verify">
        <Button type="button">Verify profile</Button>
      </Link>
    </div>
  )
}
